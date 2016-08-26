package com.att.networkadapter.cliadapter;

import com.jcraft.jsch.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class SshClient {
    private static final int SSH_PORT = 22;
    private static final int MAX_SEND_COMMAND_RETRIES = 3;
    private static String ENTER_CHARACTER = "\r";
    int removeFromTailPrompt = 0;
    private String host;
    private String userName;
    private String password;
    //making variable prompt non-static as in case of multiple unit scanning(by different threads)
//  prompt must have different value instead of one.
    private String prompt = null;
    private JSch jsch;
    private Session session;
    private ChannelShell channel;
    private ChannelSftp sftp;
    private int timeout;
    private int bufferSize;
    private int waitTime;
    private InputStream sshInputStream;
    private OutputStream sshOutputStream;
    private boolean isAIX;

// private static final Logger _logger = LoggerFactory.getLogger(SshClient.class);

    public SshClient(int timeout, int removeFromTailPrompt, int bufferSize, int waitTime) {
        this.timeout = timeout;
        this.removeFromTailPrompt = removeFromTailPrompt;
        this.bufferSize = bufferSize;
        this.waitTime = waitTime;

    }

//private boolean isConnect = false;

    public boolean connect(String host, String userName, String password) throws Exception {
        this.host = host;
        this.userName = userName;
        this.password = password;
        if (this.session != null && this.session.isConnected()) {
            disconnect();
        }
        createSession();
        getPrompt();

        return true;
    }

    private void getPrompt() throws Exception {
// ssh
        try {
            channel = (ChannelShell) this.session.openChannel("shell");

            ((ChannelShell) channel).setPtyType("vt102");
            sshInputStream = channel.getInputStream();
            sshOutputStream = channel.getOutputStream();
            channel.connect(timeout);
//channel.connect();


            String command = "show clock" + ENTER_CHARACTER;
//String command = "whoami" + ENTER_CHARACTER;
            sshOutputStream.write(command.getBytes());
            sshOutputStream.flush();
            Thread.sleep(500);

            byte[] buff = new byte[sshInputStream.available()];
            int state = sshInputStream.read(buff);
            if (state > 0) {
                String result = new String(buff);
                System.out.print(" Result " + result);
                if (result.indexOf("AIX") >= 0) {

                    isAIX = true;
// log.debug("The unit is AIX");
                }


                String[] lines = (result).split("\n");
                prompt = lines[lines.length - 1].trim();
                if (removeFromTailPrompt > 0) {
                    prompt = prompt.substring(0, prompt.length() - removeFromTailPrompt);
                }

            } else {
                System.out.println("Could not get the prompt!");
            }
        } catch (Exception e2) {
            throw new Exception("ssh not connect yet...");
        }
    }

    private void createSession() throws Exception {
        jsch = new JSch();
        this.session = jsch.getSession(userName, host, SSH_PORT);
        if (password != null) {
            session.setPassword(password);
        }
        Hashtable<String, String> config = new Hashtable<String, String>();
        config.put("StrictHostKeyChecking", "no");
        config.put("compression.s2c", "zlib,none");
        config.put("compression.c2s", "zlib,none");
        session.setConfig(config);
        session.connect(timeout);
//session.connect();
    }


    public String sendCommand(String command, long waitForCammnd) throws Exception {
        List<String> commands = new ArrayList<String>();
        commands.add(command);
        return sendCommands(commands, waitForCammnd, 1);
    }

    public String sendCommands(List<String> commands, long waitForCammnd, int retry) throws Exception {

        StringBuilder ret = new StringBuilder();
        try {
            StringBuilder buffer = new StringBuilder();


            if (!this.session.isConnected()) {
//log.info("Session is down");
                createSession();
                channel = (ChannelShell) this.session.openChannel("shell");
                ((ChannelShell) channel).setPtyType("vt102");
                sshInputStream = channel.getInputStream();
                sshOutputStream = channel.getOutputStream();
                channel.connect(timeout);
//channel.connect();
            } else if (!channel.isConnected()) {
                System.out.println("Channel is down");
                createSession();
                channel = (ChannelShell) this.session.openChannel("shell");
                ((ChannelShell) channel).setPtyType("vt102");
                sshInputStream = channel.getInputStream();
                sshOutputStream = channel.getOutputStream();
                channel.connect(timeout);
//channel.connect();
            }


            try {
                int state = 0;
                for (String command : commands) {
// send the command
                    if (command.trim().length() == 0) {
                        continue;
                    }
                    command += ENTER_CHARACTER;
                    byte[] buff = new byte[sshInputStream.available()];
                    state = sshInputStream.read(buff);
                    while (state != -1) {
//log.info("BUF 1 is "+new String(buff));
                        buffer.append(new String(buff));
                        if (sshInputStream.available() == 0) {
                            break;
                        }
                        buff = new byte[sshInputStream.available()];
                        state = sshInputStream.read(buff);
                    }

                    buffer = new StringBuilder();
//  Need to break up the output stream into chunks to get around buffer issue

                    byte[] commandBytes = command.getBytes();
//log.debug("numbytes is : " + commandBytes.length);
                    int offset = 0;
                    int chunksize = bufferSize;
                    while (offset < commandBytes.length) {
                        if (offset + chunksize > commandBytes.length)
                            chunksize = commandBytes.length - offset;
//log.debug("sending bytes from "+offset + " for "+chunksize);
                        sshOutputStream.write(commandBytes, offset, chunksize);
                        sshOutputStream.flush();
                        offset = offset + chunksize;
                        Thread.sleep(waitTime);
                    }
                    sshOutputStream.write(command.getBytes());
                    sshOutputStream.flush();
                    Thread.sleep(100);

// verify command end
                    buff = new byte[sshInputStream.available()];
                    state = sshInputStream.read(buff);
                    long timeout = waitForCammnd / 1000;
                    while (state != -1) {
//log.info("BUF 2 is "+new String(buff));
                        buffer.append(new String(buff));
                        if (sshInputStream.available() == 0) {
                            System.out.println("Checking if prompt is back for command: " + command + " buffer1: [" + buffer.toString() + "]");

//System.out.println("Checking if prompt is back for command: ");
                            if (isPromptBack(buffer)) {
//log.trace("Prompt is back");
                                break;
                            }
                            if (buffer.toString().toLowerCase().indexOf("commit database consolidation") >= 0) {
//log.debug("Waiting 10 seconds extra for commit command end");
//log.info("Waiting 10 seconds extra for commit command end");
                                Thread.sleep(5000);
                            }
                            --timeout;
                            if (timeout == 0) {

                                if ((!this.session.isConnected() || !channel.isConnected()) && retry < MAX_SEND_COMMAND_RETRIES) {
//log.info("Session or chanel is down. Attempt " + retry);
                                    return sendCommands(commands, waitForCammnd, ++retry);
                                } else {
                                    System.out.println("Timeout (" + waitForCammnd + " millisec) reached while waiting for prompt for command: " + command + " buffer: [" + buffer.toString() + "]");
                                    throw new JSchException("Timeout (" + waitForCammnd + " milisec) reached while waiting for prompt for command: " + command + " buffer: [" + buffer.toString() + "]");
                                }

                            }
                            Thread.sleep(1000);
                        }
                        buff = new byte[sshInputStream.available()];
                        state = sshInputStream.read(buff);
                    }
                    String buf = buffer.toString();
//log.debug("return buffer:\n" + buf);
                    ret.append(buf);
//System.out.println(buffer.toString());
                }

            } catch (Exception e) {
                throw e;
            }
//return buffer.toString();
            return ret.toString();
        } finally {
//			if (channel != null){
//				channel.disconnect();
//			}
//			if (sshOutputStream != null){
//				sshOutputStream.close();
//			}
//			if (sshInputStream != null){
//				sshInputStream.close();
//			}

        }
    }

    private boolean isPromptBack(StringBuilder buffer) {
        if (buffer.length() <= 0) {
            return false;
        }
// log.debug("buffer" + buffer);
        String[] lines = buffer.toString().split("\n");

        if (this.removeFromTailPrompt > 0) {
            if (lines[lines.length - 1].indexOf(prompt) >= 0) {
                return true;
            }
        } else {
            if (lines[lines.length - 1].trim().equals(prompt)) {
                if (lines[lines.length - 1].trim().substring(0, 3).equals(prompt.trim().substring(0, 3))) { //as for the fix for TIR PSG00670546 we traverse through direcotry using cd. so to recognise the prompt for example sdp1:/var/tmp/test#  with sdp1:/#
                    return true;
                }
            }
        }
        return false;
    }


    public void put(String source, String destination) throws Exception {
        sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect(timeout);
        sftp.put(source, destination);
        sftp.disconnect();

    }

    public void get(String source, String destination) throws Exception {
        sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect(timeout);
        sftp.get(source, destination);
        sftp.disconnect();
    }

    public boolean isAIX() {
        return isAIX;
    }


    public void disconnect() {
        if (this.sftp != null) {
            sftp.disconnect();
        }

        this.jsch = null;
        if (channel != null) {
            channel.disconnect();
        }
        if (sshOutputStream != null) {
            try {
                sshOutputStream.close();
            } catch (IOException e) {
            }
        }
        if (sshInputStream != null) {
            try {
                sshInputStream.close();
            } catch (IOException e) {
            }
        }

        if (session != null) {
            session.disconnect();
        }


    }


}
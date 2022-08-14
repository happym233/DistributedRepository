package server;

import socklib.ListenerInfo;
import socklib.SimpleSocketProtocol;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ERAP extends SimpleSocketProtocol {
    private Directory dictionary;
    private PeerConnections connections;
    private String id;

    public ERAP(Socket s, Directory directory, PeerConnections connections, String id, ListenerInfo info) {
        super(s, info);
        this.dictionary = directory;
        this.connections = connections;
        this.id = id;
    }

    private ArrayList<Integer> parse_str_to_ints(String str) {
        String[] strs = str.split(",");
        ArrayList<Integer> ints = new ArrayList<>();
        for(String s:strs) {
            ints.add(Integer.parseInt(s));
        }
        return ints;
    }

    @Override
    public void run() throws IOException {
        sendln("OK Repository <<" + id + ">> ready");
        while (isRunning() && isConnected()) {
            String data = recvln();
            String[] input = data.split(" ");
            Integer input_len = input.length;
            String operation = input[0];
            String key = "";
            String[] keys = null;
            Integer val = -1;
            PeerConnector connector = null;


            switch (operation.toUpperCase()) {
                case "ADD":
                    if (input_len == 3) {
                        try {
                            val = Integer.parseInt(input[2]);
                            keys = input[1].split("\\.");
                            if (keys.length == 1) {
                                dictionary.add(input[1], val);
                                sendln("OK");
                            } else if (keys.length == 2) {
                                if (keys[0].equals(id)) {
                                    dictionary.add(keys[1], val);
                                    sendln("OK");
                                } else {
                                    connector = connections.getConnections().get(keys[0]);
                                    System.out.println(keys[0]);
                                    if (connector != null) {
                                        int[] res = connector.fetch(operation +" " + keys[1] + " "+ input[2]);
                                        sendln("OK");
                                    }
                                    else {
                                        sendln("UNKNOWN REPO-ID");
                                    }
                                }
                            } else {
                                sendln("INVALID INPUT");
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                            sendln("INVALID INPUT");
                        }
                    } else {
                        sendln("INVALID INPUT");
                    }
                    break;

                case "SET":
                case "UPDATE":
                    if (input_len == 3) {
                        try {
                            boolean success = false;
                            keys = input[1].split("\\.");
                            if (keys.length == 1) {
                                success = dictionary.set(input[1], parse_str_to_ints(input[2]));
                            } else if (keys.length == 2) {
                                if (keys[0].equals(id)) {
                                    success  = dictionary.set(keys[1], parse_str_to_ints(input[2]));
                                } else {
                                    connector = connections.getConnections().get(keys[0]);
                                    if (connector != null) {
                                        int[] res = connector.fetch(operation + " " + keys[1] + " " + input[2]);
                                        if (res != null) {
                                            success = true;
                                        }
                                    } else {
                                        sendln("UNKNOWN REPO-ID");
                                        continue;
                                    }
                                }
                            }
                            if (success) {
                                sendln("OK");
                            } else {
                                sendln("UNKNOWN KEY");
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                            sendln("INVALID INPUT");
                        }
                    } else {
                        sendln("INVALID INPUT");
                    }
                    break;

                case "DELETE":
                    if (input_len == 2) {
                        try {
                            Boolean success = false;
                            keys = input[1].split("\\.");
                            if (keys.length == 1) {
                                success = dictionary.delete(input[1]);
                            } else if (keys.length == 2) {
                                if (keys[0].equals(id)) {
                                    success = dictionary.delete(keys[1]);
                                } else {
                                    connector = connections.getConnections().get(keys[0]);
                                    if (connector != null) {
                                        int[] res = connector.fetch(operation + " " + keys[1]);
                                        if (res != null) {
                                            success = true;
                                        }
                                    } else {
                                        sendln("UNKNOWN REPO-ID");
                                        continue;
                                    }
                                }
                            }
                            if (success) {
                                sendln("OK");
                            } else {
                                sendln("UNKNOWN KEY");
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                            sendln("INVALID INPUT");
                        }
                    } else {
                        sendln("INVALID INPUT");
                    }
                    break;

                case "LIST":
                    if (input_len == 1) {
                        sendln("OK " + dictionary.list());
                    } else if (input_len == 2) {
                        key = input[1];
                        if (key.equals(id)) {
                            sendln("OK " + dictionary.list());
                        } else {
                            connector = connections.getConnections().get(key);
                            if (connector != null) {
                                connector.fetch(operation);
                                sendln(connector.getLastResult());
                            }
                            else {
                                sendln("UNKNOWN REPO-ID");
                            }
                        }
                    } else {
                        sendln("INVALID INPUT");
                    }
                    break;

                case "GET":
                    if (input_len == 2) {
                        try {
                            keys = input[1].split("\\.");
                            if (keys.length == 1) {
                                 sendln(dictionary.getValue(input[1]));
                            } else if (keys.length == 2) {
                                if (keys[0].equals(id)) {
                                    sendln(dictionary.getValue(input[1]));
                                } else {
                                    connector = connections.getConnections().get(keys[0]);
                                    if (connector != null) {
                                        int[] res = connector.fetch(operation + " " + keys[1]);
                                        sendln(connector.getLastResult());
                                    } else {
                                        sendln("UNKNOWN REPO-ID");
                                        continue;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                            sendln("INVALID INPUT");
                        }
                    } else {
                        sendln("INVALID INPUT");
                    }
                    break;

                case "GETA":
                    if (input_len == 2) {
                        try {
                            keys = input[1].split("\\.");
                            if (keys.length == 1) {
                                sendln(dictionary.getValues(input[1]));
                            } else if (keys.length == 2) {
                                if (keys[0].equals(id)) {
                                    sendln(dictionary.getValues(input[1]));
                                } else {
                                    connector = connections.getConnections().get(keys[0]);
                                    if (connector != null) {
                                        int[] res = connector.fetch(operation + " " + keys[1]);
                                        sendln(connector.getLastResult());
                                    } else {
                                        sendln("UNKNOWN REPO-ID");
                                        continue;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                            sendln("INVALID INPUT");
                        }
                    } else {
                        sendln("INVALID INPUT");
                    }
                    break;

                case "MAX":
                    if (input_len == 2) {
                        try {
                            keys = input[1].split("\\.");
                            if (keys.length == 1) {
                                sendln(dictionary.max(input[1]));
                            } else if (keys.length == 2) {
                                if (keys[0].equals(id)) {
                                    sendln(dictionary.max(input[1]));
                                } else {
                                    connector = connections.getConnections().get(keys[0]);
                                    if (connector != null) {
                                        int[] res = connector.fetch(operation + " " + keys[1]);
                                        sendln(connector.getLastResult());
                                    } else {
                                        sendln("UNKNOWN REPO-ID");
                                        continue;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                            sendln("INVALID INPUT");
                        }
                    } else {
                        sendln("INVALID INPUT");
                    }
                    break;
                case "MIN":
                    if (input_len == 2) {
                        try {
                            keys = input[1].split("\\.");
                            if (keys.length == 1) {
                                sendln(dictionary.min(input[1]));
                            } else if (keys.length == 2) {
                                if (keys[0].equals(id)) {
                                    sendln(dictionary.min(input[1]));
                                } else {
                                    connector = connections.getConnections().get(keys[0]);
                                    if (connector != null) {
                                        int[] res = connector.fetch(operation + " " + keys[1]);
                                        sendln(connector.getLastResult());
                                    } else {
                                        sendln("UNKNOWN REPO-ID");
                                        continue;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                            sendln("INVALID INPUT");
                        }
                    } else {
                        sendln("INVALID INPUT");
                    }
                    break;

                case "SUM":
                    if (input_len == 2) {
                        try {
                            keys = input[1].split("\\.");
                            if (keys.length == 1) {
                                sendln(dictionary.sum(input[1]));
                            } else if (keys.length == 2) {
                                if (keys[0].equals(id)) {
                                    sendln(dictionary.sum(input[1]));
                                } else {
                                    connector = connections.getConnections().get(keys[0]);
                                    if (connector != null) {
                                        int[] res = connector.fetch(operation + " " + keys[1]);
                                        sendln(connector.getLastResult());
                                    } else {
                                        sendln("UNKNOWN REPO-ID");
                                        continue;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                            sendln("INVALID INPUT");
                        }
                    } else {
                        sendln("INVALID INPUT");
                    }
                    break;
                case "RESET":
                    if (input_len == 1) {
                        dictionary.reset();
                        sendln("OK");
                    } else if (input_len == 2) {
                        key = input[1];
                        if (key.equals(id)) {
                            dictionary.reset();
                            sendln("OK");
                        } else {
                            connector = connections.getConnections().get(key);
                            if (connector != null) {
                                int[] res = connector.fetch(operation);
                                sendln("OK");
                            }
                            else {
                                sendln("UNKNOWN REPO-ID");
                            }
                        }
                    } else {
                        sendln("INVALID INPUT");
                    }
                    break;

                case "DSUM":
                    if (input_len >= 4 && input[2].toUpperCase(Locale.ROOT).equals("INCLUDING")) {
                        key = input[1];
                        int sum = 0;
                        String res;
                        for (int i = 3; i < input_len; i++) {
                            if (input[i].equals(id)) {
                                res = dictionary.sum(key);
                            } else {
                                connector = connections.getConnections().get(input[i]);
                                if (connector != null) {
                                    connector.fetch("SUM " + key);
                                    res = connector.getLastResult();
                                }
                                else {
                                    sendln("UNKNOWN REPO-ID");
                                    continue;
                                }
                            }
                            String[] split_res = res.split(" ");
                            if (!split_res[0].equals("OK")) {
                                sendln(res + " IN <<" + input[i] + ">>");
                            } else {
                                sum += Integer.parseInt(split_res[1]);
                            }
                        }
                        sendln("OK " + sum);
                    } else {
                        sendln("INVALID INPUT");
                    }
                    break;

                case "EXIT":
                    sendln("BYE, it was nice seeing you.");
                    close();
                    return;
                default:
                    sendln("ERR: Sorry This key is nonexistent in the collection, please check again. " +
                            "Say EXIT if you wish to exit.");
            }

        }
    }

}

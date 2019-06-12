package com.project.labautomationsystem.client;
import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import com.project.labautomationsystem.finddrive.FindDrive;
public class Client extends javax.swing.JFrame {
  
    String username, address = "localhost";
    ArrayList<String> users = new ArrayList();
    int port = 2222;
    Boolean isConnected = false;
    Socket sock;
    BufferedReader reader;
    PrintWriter writer;
    File file;
    DefaultListModel model;
    DefaultListModel fmodel;
    DefaultTableModel tmodel;
    DefaultTreeModel tree;
    MutableTreeNode root;
    static int sun=0;
    static String oj="";
    
    
    public void ListenThread() 
    {
         Thread IncomingReader = new Thread(new IncomingReader());
         IncomingReader.start();
    }
    
    //--------------------------//
    
    public void userAdd(String data) 
    {
         users.add(data);
    }
    
    //--------------------------//
    
    public void userRemove(String data) 
    {
         txtBody.append(data + " is now offline.\n");
    }
    
    //--------------------------//
    
    public void writeUsers() 
    {
         String[] tempList = new String[(users.size())];
         users.toArray(tempList);
         for (String token:tempList) 
         {
             //users.add(token + "\n");
         }
    }
    
    //--------------------------//
    
    public void sendDisconnect() 
    {
        String univ = txtName.getText();
        String cour = (String) txtClRoll.getText();
        String yr = (String) txtLabCd.getText();
        String sec = (String) section.getSelectedItem();
        String gr = (String) group.getSelectedItem();
        String bye = ("disconnect`"+txtUser.getText()+"`"+"Name: "+univ+" Class Roll No.: "+cour+" For Lab: "+yr+" Of Section: "+sec+" Group: "+gr+"\n");
        try
        {
            writer.println(bye); 
            writer.flush(); 
            Disconnect();
        } catch (Exception e) 
        {
            txtBody.append("Could not send Disconnect message.\n");
        }
    }

    //--------------------------//
    
    public void Disconnect() 
    {
        try 
        {
            txtBody.append("Disconnected.\n");
            sock.close();
            txtAtt.enable(false);
                txtMsg.setEditable(false);
                txtFile.setEditable(false);
                btnSend.enable(false);
        } catch(Exception ex) {
            txtBody.append("Failed to disconnect. \n");
        }
        isConnected = false;
        txtUser.setEditable(true);
        //txtPass.setEditable(true);
        btnLogin.setEnabled(true);
        btnDisconnect.setEnabled(false);

    }
    //--------------------------//
    
    public class IncomingReader implements Runnable
    {
        @Override
        public void run() 
        {
            String[] data,users;
            String stream;
            int length=0;
            Runtime r=Runtime.getRuntime();

            try 
            {
                while ((stream = reader.readLine()) != null) 
                {
                     data = stream.split("`");

                     if (data[0].equals("CH")) 
                     {  
                        if(data[2].equals("All") || data[2].equals(txtUser.getText()) || data[1].equals(txtUser.getText()))
                        {
                            txtBody.append(data[1] + "->" + data[2] + " : "+ data[3] +"\n");
                            txtBody.setCaretPosition(txtBody.getDocument().getLength());
                        }                        
                     }                    
                     else if (data[0].equals("login"))
                     {
                        txtBody.append(data[1]+" joined\n");
                        if(!data[1].equals(txtUser.getText()))
                            model.addElement(data[1]);
                          
                     } 
                     else if (data[0].equals("disconnect")) 
                     {
                         txtBody.append(data[1]+" has left\n");
                         int i=0;
                         while(true)
                         {
                            if(model.getElementAt(i).toString().equals(data[1]))
                               break;
                            i++;
                         }
                         model.remove(i);
                    
                     } 
                     else if(data[0].equals("online") && data[1].equals(txtUser.getText()))
                     {
                         //txtBody.append(data[2]);
                         
                         users=data[2].split(",");
                         int i=0;
                         length=0;
                         while(i<data[2].length())
                         {   if(data[2].charAt(i)==',')
                                 length++;
                             i++;
                         }
                         i=0;
                         model.clear();
                         model.addElement("All");
                         while(i<length)
                         {
                            model.addElement(users[i]);
                            i++;
                         }
                         listUser.setModel(model);
                         listUser.setSelectedIndex(0);
                     }
                     else if(data[0].equals("NS"))
                     {   tmodel.getDataVector().removeAllElements();
                         String[] notes=data[1].split("~");
                         int i=0;
                         String a;
                         String[] note=notes[i].split("#@#");
                            for(int j=0;j<notes.length;j++)
                            {   note=notes[i].split("#@#");
                                tmodel.addRow(note);
                                i++;
                            }
                     }
                     else if(data[0].equals("DF"))
                     {  
                        root=new DefaultMutableTreeNode("Questions");
                        int i=0;
                        MutableTreeNode q=null,a=null;
                        String[] notes=data[1].split("~");
                        String[] note=notes[i].split("#@#");
                            for(int j=0;j<notes.length;j++)
                            {   note=notes[i].split("#@#");
                                q=new DefaultMutableTreeNode("Q"+(i+1)+". "+note[0]);
                                for(int k=2;k<note.length;k++)
                                {    a=new DefaultMutableTreeNode(note[k]);
                                     q.insert(a,k-2);
                                }
                                root.insert(q,i);
                                i++;
                            }
                        tree=new DefaultTreeModel(root);
                        jTree1.setModel(tree);
                     }
                     else if(data[0].equals("FT"))
                     {  
                        String[] file=stream.split("#@#@#");
                        file[0]=file[0].substring(3);
                        if(file[1].equals(txtUser.getText()) || file[1].equals("All") && !file[0].equals((txtUser.getText())))
                        {
                        byte[] decodedBytes = Base64.getDecoder().decode(file[3]);
                        String home = System.getProperty("user.home");
			OutputStream out = new FileOutputStream(home+"/Downloads/"+file[2]);
			out.write(decodedBytes);
                        txtBody.append(file[0]+"->"+file[1]+" : "+file[2]+"\n"+file[2]+" has been saved to Downloads\n");
                        out.close();
                        }
                     }
                     else if(data[0].equals("FS"))
                     {  int i=0;
                        String[] aa=data[1].split("#@#");
                        fmodel.clear();
                        while(i<aa.length)
                        {
                           fmodel.addElement(aa[i]);
                           i++;
                        }
                        listFile.setModel(fmodel);
                        listFile.setSelectedIndex(0);
                     }
                     else if(data[0].equals("FSR"))
                     {
                         if(data[1].equals(txtUser.getText()))
                         {
                            byte[] decodedBytes = Base64.getDecoder().decode(data[3]);
                            String home = System.getProperty("user.home");
                            OutputStream out = new FileOutputStream(home+"/Downloads/"+data[2]);
                            out.write(decodedBytes);
                            out.close();
                            JOptionPane.showMessageDialog(null,data[2]+" has been saved to Downloads","",JOptionPane.INFORMATION_MESSAGE);
                            
                         }
                     }
                     else if(data[0].equals("DisconnectAll"))
                     {
                         Disconnect();
                         txtBody.append("Server Refreshed!!\nPlease login again\n");
                     }
                     else if(data[0].equals("DisconnectAllReset"))
                     {
                         Disconnect();
                         txtBody.append("Server Reset!!\nPlease login again\n");
                     }
                     else if(data[0].equals("ShutDown"))
                     {
                          r.exec("shutdown -s");
                     }
                     else if(data[0].equals("Restart"))
                     {
                          r.exec("shutdown -r");
                     }
                     else if(data[0].equals("LogOff"))
                     {
                          r.exec("C:\\Windows\\System32\\rundll32.exe user32.dll,LockWorkStation -t 60");
                     }
                     else if(data[0].equals("FSW"))
                     {
                         writer.println("FSW` ");
                     }
                    
                     else if(data[0].equals("StartWhite"))
                     {
                         Client c = new Client();
                         c.sun = 1;
                     } 
                     else if(data[0].equals("Drive"))
                     {
                          FindDrive f = new FindDrive();
                          f.find();
                     }
                     else if(data[0].equals("Disable"))
                     {
                         
                        if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
                        {
                        Process pb = Runtime.getRuntime().exec("netsh interface set interface \"Local Area Connection\" admin=DISABLE");

                        String line;
                        BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));
                        while ((line = input.readLine()) != null)
                        {
                            System.out.println(line);
                        }
                        input.close();
                        }
                        else
                        {
                        Process pb = Runtime.getRuntime().exec("sudo ifconfig eth0 down");

                        String line;
                        BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));
                        while ((line = input.readLine()) != null)
                        {
                            System.out.println(line);
                        }
                        input.close();
                        }
                     }
                     else if(data[0].equals("Enable"))
                     {
                        if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
                        {
                        Process pb = Runtime.getRuntime().exec("netsh interface set interface \"Local Area Connection\" admin=ENABLE");

                        String line;
                        BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));
                        while ((line = input.readLine()) != null)
                        {
                            System.out.println(line);
                        }
                        input.close();
                        }
                        else
                        {
                        Process pb = Runtime.getRuntime().exec("sudo ifconfig eth0 up");

                        String line;
                        BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));
                        while ((line = input.readLine()) != null)
                        {
                            System.out.println(line);
                        }
                        input.close();
                        }
                     }
                }
           }catch(Exception ex) { }
        }
    }

    /**
     * Creates new form Client
     */
    public Client()
    {
        initComponents();
        tmodel.addColumn("Uploaded by");
        tmodel.addColumn("Title");
        tmodel.addColumn("Content");
        this.setTitle("Lab Automation System - Messenger");  
       
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel7 = new javax.swing.JPanel();
        txtHname = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtHport = new javax.swing.JTextField();
        btnConnect = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtUser = new javax.swing.JTextField();
        btnLogin = new javax.swing.JButton();
        btnDisconnect = new javax.swing.JButton();
        txtAtt = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtBody = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        listUser = new javax.swing.JList();
        jLabel5 = new javax.swing.JLabel();
        txtMsg = new javax.swing.JTextField();
        btnSend = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        txtFile = new javax.swing.JTextField();
        btnBrowse = new javax.swing.JButton();
        btnSendf = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        listFile = new javax.swing.JList<>();
        btnShareFile = new javax.swing.JButton();
        btnGetFile = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtNote = new javax.swing.JTextArea();
        btnShare = new javax.swing.JButton();
        txtTitle = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        txtQues = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        btnAsk = new javax.swing.JButton();
        btnQuora = new javax.swing.JButton();
        btnGoogle = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        txtQuesNo = new javax.swing.JTextField();
        btnSubmit = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        txtAns = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        adminPanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtUserName = new javax.swing.JTextField();
        txtAdminPass = new javax.swing.JPasswordField();
        btnRefresh = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        submit = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        section = new javax.swing.JComboBox<>();
        jLabel19 = new javax.swing.JLabel();
        group = new javax.swing.JComboBox<>();
        txtClRoll = new javax.swing.JTextField();
        txtLabCd = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowDeactivated(java.awt.event.WindowEvent evt) {
                formWindowDeactivated(evt);
            }
        });

        txtHname.setText("localhost");
        txtHname.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtHnameKeyPressed(evt);
            }
        });

        jLabel1.setText("Host Address : ");

        jLabel2.setText("Host Port : ");

        txtHport.setText("51017");
        txtHport.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtHportKeyPressed(evt);
            }
        });

        btnConnect.setText("Connect");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        jLabel4.setText("Username :");

        txtUser.setEditable(false);
        txtUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUserActionPerformed(evt);
            }
        });
        txtUser.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtUserKeyPressed(evt);
            }
        });

        btnLogin.setText("Login");
        btnLogin.setEnabled(false);
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        btnDisconnect.setText("Disconnect");
        btnDisconnect.setEnabled(false);
        btnDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisconnectActionPerformed(evt);
            }
        });

        txtAtt.setEnabled(false);
        txtAtt.setName(""); // NOI18N

        jPanel1.setEnabled(false);

        txtBody.setColumns(20);
        txtBody.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        txtBody.setRows(5);
        txtBody.setEnabled(false);
        jScrollPane1.setViewportView(txtBody);

        listUser.setModel((model = new DefaultListModel()));
        jScrollPane2.setViewportView(listUser);

        jLabel5.setText("Message : ");

        txtMsg.setEditable(false);
        txtMsg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtMsgKeyPressed(evt);
            }
        });

        btnSend.setText("Send Message ");
        btnSend.setEnabled(false);
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });

        jLabel6.setText("File :");

        txtFile.setEditable(false);
        txtFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFileActionPerformed(evt);
            }
        });

        btnBrowse.setText("...");
        btnBrowse.setEnabled(false);
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        btnSendf.setText("Send");
        btnSendf.setEnabled(false);
        btnSendf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendfActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFile, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSendf, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtMsg)
                        .addGap(18, 18, 18)
                        .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(7, 7, 7))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSend)
                    .addComponent(jLabel5)
                    .addComponent(txtMsg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(btnSendf, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnBrowse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6)
                    .addComponent(txtFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txtAtt.addTab("Chat", jPanel1);

        listFile.setModel((fmodel = new DefaultListModel()));
        jScrollPane7.setViewportView(listFile);

        btnShareFile.setText("Share a file");
        btnShareFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShareFileActionPerformed(evt);
            }
        });

        btnGetFile.setText("Get this file");
        btnGetFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGetFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnShareFile, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                    .addComponent(btnGetFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnShareFile, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnGetFile, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        txtAtt.addTab("File Share", jPanel2);

        jTable2.setModel((tmodel=new DefaultTableModel()));
        jScrollPane4.setViewportView(jTable2);

        jLabel7.setText("Share a Note:");

        txtNote.setColumns(20);
        txtNote.setRows(5);
        jScrollPane3.setViewportView(txtNote);

        btnShare.setText("Share");
        btnShare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShareActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(18, 18, 18)
                                .addComponent(txtTitle))
                            .addComponent(jScrollPane3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnShare)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 103, Short.MAX_VALUE)
                        .addComponent(btnShare)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        txtAtt.addTab("Notes", jPanel3);

        jScrollPane5.setViewportView(jTree1);

        jLabel8.setText("Ask a question:");

        btnAsk.setText("Ask to Portal");
        btnAsk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAskActionPerformed(evt);
            }
        });

        btnQuora.setText("Search on Quora");
        btnQuora.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuoraActionPerformed(evt);
            }
        });

        btnGoogle.setText("Search on Google");
        btnGoogle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoogleActionPerformed(evt);
            }
        });

        jLabel9.setText("Answer a Question: ");

        btnSubmit.setText("Submit");
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });

        txtAns.setColumns(20);
        txtAns.setRows(5);
        jScrollPane6.setViewportView(txtAns);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtQues))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(0, 213, Short.MAX_VALUE)
                        .addComponent(btnGoogle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnQuora)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAsk))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtQuesNo, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnSubmit))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane6)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtQues, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGoogle)
                    .addComponent(btnQuora)
                    .addComponent(btnAsk))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(txtQuesNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSubmit))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        txtAtt.addTab("Discuss", jPanel4);

        jButton2.setText("Admin Panel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel10.setText("UserName");

        jLabel11.setText("Password");

        btnRefresh.setText("Refresh the Server");
        btnRefresh.setEnabled(false);
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        btnReset.setText("Reset the Server");
        btnReset.setEnabled(false);
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        jButton1.setText("Submit");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout adminPanelLayout = new javax.swing.GroupLayout(adminPanel);
        adminPanel.setLayout(adminPanelLayout);
        adminPanelLayout.setHorizontalGroup(
            adminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(adminPanelLayout.createSequentialGroup()
                .addGroup(adminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(adminPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnReset, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE))
                    .addGroup(adminPanelLayout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addGroup(adminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton1)
                            .addGroup(adminPanelLayout.createSequentialGroup()
                                .addGroup(adminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel11))
                                .addGap(18, 18, 18)
                                .addGroup(adminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtUserName)
                                    .addComponent(txtAdminPass, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        adminPanelLayout.setVerticalGroup(
            adminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(adminPanelLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(adminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(adminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtAdminPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addGroup(adminPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReset)
                    .addComponent(btnRefresh))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE)
                .addComponent(adminPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2)
                    .addComponent(adminPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(150, Short.MAX_VALUE))
        );

        adminPanel.setVisible(false);

        txtAtt.addTab("Tools", jPanel5);

        submit.setText("Submit");
        submit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel15.setText("Name :");

        jLabel16.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel16.setText("Section:");

        jLabel17.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel17.setText("Class Roll No. :");

        jLabel18.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(153, 0, 204));
        jLabel18.setText("Lab Attendance Record");

        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });

        section.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "A", "B", "C", "D", "E", "F", "G", "H" }));
        section.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sectionActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel19.setText("Lab Code :");

        group.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "I", "II", "III", " " }));

        txtClRoll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtClRollActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(120, 120, 120)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(61, 61, 61)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(submit, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                            .addComponent(section, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                            .addComponent(group, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(txtName, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtClRoll, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtLabCd, javax.swing.GroupLayout.Alignment.LEADING)))
                .addContainerGap(125, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(126, 126, 126))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtClRoll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                    .addComponent(txtLabCd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(section, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(group, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(submit, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        txtAtt.addTab("Attendance", jPanel6);

        jButton3.setText("Start WhiteBorad");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(237, 237, 237)
                .addComponent(jButton3)
                .addContainerGap(224, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jButton3)
                .addContainerGap(351, Short.MAX_VALUE))
        );

        txtAtt.addTab("WhiteBorad", jPanel8);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtAtt)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtHname)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtHport))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(39, 39, 39)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnDisconnect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnConnect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtHname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(txtHport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnConnect))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(btnLogin))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDisconnect)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAtt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public boolean hostAvailabilityCheck() { 
    try (Socket s = new Socket(txtHname.getText(), Integer.parseInt(txtHport.getText()))){
        return true;
    }catch (Exception ex) {
        /* ignore */
    }
    return false;
    }
    
    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
try{
        if(hostAvailabilityCheck()==true)
        {
            txtUser.setEditable(true);
            //txtPass.setEditable(true);
            btnLogin.setEnabled(true);
            txtHname.setEditable(false);
            txtHport.setEditable(false);
            btnConnect.setEnabled(false);
            
        }
        else
        {
            txtBody.append("Cannot Connect! Try Again. \n");
        }
    }catch(Exception e)
    {
        txtBody.append("Cannot Connect! Try Again. \n");
    }        
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        //txtUser.setText( txtUser.getText().substring(0,1).toUpperCase() + txtUser.getText().substring(1));
        if(txtUser.getText().equals(""))
        {
            JOptionPane.showMessageDialog(null,"Credentails Required..!!","",JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try 
            {
                txtAtt.setEnabled(true);
                sock = new Socket(txtHname.getText(), Integer.parseInt(txtHport.getText()));
                InputStreamReader streamreader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(streamreader);
                writer = new PrintWriter(sock.getOutputStream());
                writer.println("login`"+txtUser.getText());
                writer.flush(); 
                isConnected = true; 
                btnSend.setEnabled(true);
                btnBrowse.setEnabled(true);
                txtUser.setEditable(false);
                btnDisconnect.setEnabled(true);
                btnLogin.setEnabled(false);
                txtBody.append("Connected..!!\n");
                username=txtUser.getText();
                txtMsg.setEditable(true);
                txtFile.setEditable(true);
            } 
            catch (NumberFormatException | IOException ex) 
            {
                txtBody.append("Cannot Connect! Try Again. \n");
                txtUser.setEditable(true);
            }
            
           ListenThread();
    }//GEN-LAST:event_btnLoginActionPerformed

    private void btnDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisconnectActionPerformed
                 sendDisconnect();      
    }//GEN-LAST:event_btnDisconnectActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
    
    }//GEN-LAST:event_formWindowClosed

    private void formWindowDeactivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowDeactivated
     // TODO add your handling code here:
    }//GEN-LAST:event_formWindowDeactivated

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    sendDisconnect();    // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosing

    private void txtUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUserActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtUserActionPerformed

    private void txtHportKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtHportKeyPressed
        if (evt.getKeyCode()==KeyEvent.VK_ENTER){
        btnConnect.doClick();
        }
    }//GEN-LAST:event_txtHportKeyPressed

    private void txtHnameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtHnameKeyPressed
        if (evt.getKeyCode()==KeyEvent.VK_ENTER){
        btnConnect.doClick();
        }
    }//GEN-LAST:event_txtHnameKeyPressed

    private void txtUserKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtUserKeyPressed
        if (evt.getKeyCode()==KeyEvent.VK_ENTER){
        btnLogin.doClick();
        }
    }//GEN-LAST:event_txtUserKeyPressed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if(txtUserName.getText().equals("admin") && txtAdminPass.getText().equals("12345"))
        {
            btnRefresh.setEnabled(true);
            btnReset.setEnabled(true);

        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        writer.println("RESET` ");
        adminPanel.setVisible(false);
        writer.flush();
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        writer.println("REFRESH` ");
        adminPanel.setVisible(false);
        writer.flush();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        adminPanel.setVisible(true);        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        String a="DFA`"+txtQuesNo.getText()+"`"+txtAns.getText()+"          @"+txtUser.getText();
        a=a.replaceAll("[\n\r]", " ");
        writer.println(a);
        writer.flush();
    }//GEN-LAST:event_btnSubmitActionPerformed

    private void btnGoogleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoogleActionPerformed
        String urlString="http://www.google.com/";
        urlString+="search?q=";
        String ques=txtQues.getText();
        String[] data=ques.split(" ");
        ques="";
        int i;
        for(i=0;i<data.length;i++)
        ques+=data[i]+"+";
        urlString+=ques;
        try {
            Desktop.getDesktop().browse(new URL(urlString).toURI());
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnGoogleActionPerformed

    private void btnQuoraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuoraActionPerformed
        String urlString="http://www.quora.com/";
        urlString+="search?q=";
        String ques=txtQues.getText();
        String[] data=ques.split(" ");
        ques="";
        int i;
        for(i=0;i<data.length;i++)
        ques+=data[i]+"+";
        urlString+=ques;
        try {
            Desktop.getDesktop().browse(new URL(urlString).toURI());
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnQuoraActionPerformed

    private void btnAskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAskActionPerformed
        String out="DF`"+txtQues.getText()+"            @"+txtUser.getText()+"` `~";
        writer.println(out);
        writer.flush();
        txtQues.setText("");
    }//GEN-LAST:event_btnAskActionPerformed

    private void btnShareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShareActionPerformed
        txtTitle.setText( txtTitle.getText().substring(0,1).toUpperCase() + txtTitle.getText().substring(1));
        try{
            String a=txtNote.getText().replaceAll("[\n\r]", " ");
            writer.println("NS`"+txtUser.getText()+"` "+txtTitle.getText()+"` "+a);
            writer.flush();
            txtTitle.setText("");
            txtNote.setText("");
        }catch(Exception e)
        {}
    }//GEN-LAST:event_btnShareActionPerformed

    private void btnGetFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetFileActionPerformed
        writer.println("FSR`"+txtUser.getText()+"`"+listFile.getSelectedValue());
        writer.flush();
    }//GEN-LAST:event_btnGetFileActionPerformed

    private void btnShareFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShareFileActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showDialog(this, "Select File");
        file = fileChooser.getSelectedFile();
        try {
            byte[] byteArray = new byte[(int) file.length()];
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(byteArray);
            String base64String = Base64.getEncoder().encodeToString(byteArray);
            writer.println("FS`"+file.getName()+"#@#@#"+base64String);
            writer.flush();
            fileInputStream.close();
            file=null;
        }catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        }
        catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        }
    }//GEN-LAST:event_btnShareFileActionPerformed

    private void btnSendfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendfActionPerformed
        try {
            byte[] byteArray = new byte[(int) file.length()];
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(byteArray);
            String base64String = Base64.getEncoder().encodeToString(byteArray);
            writer.println("FT`"+txtUser.getText()+"#@#@#"+listUser.getSelectedValue().toString()+"#@#@#"+file.getName()+"#@#@#"+base64String);
            writer.flush();
            fileInputStream.close();
            txtFile.setText("");
            file=null;
            btnSendf.setEnabled(false);
        }catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        }
        catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        }
    }//GEN-LAST:event_btnSendfActionPerformed

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showDialog(this, "Select File");
        file = fileChooser.getSelectedFile();

        if(file != null){
            if(!file.getName().isEmpty()){
                btnSendf.setEnabled(true);
                String str;

                if(txtFile.getText().length() > 30){
                    String t = file.getPath();
                    str = t.substring(0, 20) + " [...] " + t.substring(t.length() - 20, t.length());
                }
                else{
                    str = file.getPath();
                }
                txtFile.setText(str);
            }
        }
    }//GEN-LAST:event_btnBrowseActionPerformed

    private void txtFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFileActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFileActionPerformed

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
        String nothing = "";
        if ((txtMsg.getText()).equals(nothing)) {
            txtMsg.setText("");
            txtMsg.requestFocus();
        } else {
            try {
                writer.println("CH`"+txtUser.getText()+"`"+listUser.getSelectedValue().toString()+"`"+txtMsg.getText());
                writer.flush(); // flushes the buffer
            } catch (Exception ex) {
                txtBody.append("Message was not sent. \n");
            }
            txtMsg.setText("");
            txtMsg.requestFocus();
        }

        txtMsg.setText("");
        txtMsg.requestFocus();
    }//GEN-LAST:event_btnSendActionPerformed

    private void txtMsgKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMsgKeyPressed
        if (evt.getKeyCode()==KeyEvent.VK_ENTER){
            btnSend.doClick();
        }
    }//GEN-LAST:event_txtMsgKeyPressed

    private void submitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitActionPerformed
        // TODO add your handling code here:
        oj=txtName.getText();
        String univ = txtName.getText();
        String cour = (String) txtClRoll.getText();
        String yr = (String) txtLabCd.getText();
        String sec = (String) section.getSelectedItem();
        String gr = (String) group.getSelectedItem();

        String Data = "Name: "+univ+" Class Roll No.: "+cour+" For Lab: "+yr+" Of Section: "+sec+" Group: "+gr+"\n";
        InputStreamReader streamreader = null;
        try {
            streamreader = new InputStreamReader(sock.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
                reader = new BufferedReader(streamreader);
        try {
            writer = new PrintWriter(sock.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
                writer.println("Attendance`"+Data);
                writer.flush(); 
                txtName.setEditable(false);
                txtClRoll.setEditable(false);
                txtLabCd.setEditable(false);
                section.setEnabled(false);
                group.setEnabled(false);
                submit.setEnabled(false);
    }//GEN-LAST:event_submitActionPerformed

    private void sectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sectionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sectionActionPerformed

    private void txtClRollActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtClRollActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtClRollActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
       if(Client.sun==1)
        {
             try {
                     URI uri= new URI("https://vrobbi-nodedrawing.herokuapp.com/#faculty");
                     java.awt.Desktop.getDesktop().browse(uri);
              
                 } 
             catch (Exception e) 
             {
             }   
        }
        else
        {
            JOptionPane.showMessageDialog(null,"Sorry faculty has not started.");
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNameActionPerformed
  public String getInetAddress() throws UnknownHostException
  {   
      InetAddress addr = InetAddress.getLocalHost();
        return txtUser.getText()+"=>"+InetAddress.getLocalHost();
  }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Client().setVisible(true);
            }
        });
    }
    
    //<----------------------Coding for NOTE SHARING------------------------------->
    
    void initNotes()
    {
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel adminPanel;
    private javax.swing.JButton btnAsk;
    public javax.swing.JButton btnBrowse;
    public javax.swing.JButton btnConnect;
    private javax.swing.JButton btnDisconnect;
    private javax.swing.JButton btnGetFile;
    private javax.swing.JButton btnGoogle;
    public javax.swing.JButton btnLogin;
    private javax.swing.JButton btnQuora;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnReset;
    public javax.swing.JButton btnSend;
    public javax.swing.JButton btnSendf;
    private javax.swing.JButton btnShare;
    private javax.swing.JButton btnShareFile;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JComboBox<String> group;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable jTable2;
    private javax.swing.JTree jTree1;
    private javax.swing.JList<String> listFile;
    public javax.swing.JList listUser;
    private javax.swing.JComboBox<String> section;
    private javax.swing.JButton submit;
    private javax.swing.JPasswordField txtAdminPass;
    private javax.swing.JTextArea txtAns;
    private javax.swing.JTabbedPane txtAtt;
    public javax.swing.JTextArea txtBody;
    private javax.swing.JTextField txtClRoll;
    public javax.swing.JTextField txtFile;
    public javax.swing.JTextField txtHname;
    public javax.swing.JTextField txtHport;
    private javax.swing.JTextField txtLabCd;
    public javax.swing.JTextField txtMsg;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextArea txtNote;
    private javax.swing.JTextField txtQues;
    private javax.swing.JTextField txtQuesNo;
    private javax.swing.JTextField txtTitle;
    public javax.swing.JTextField txtUser;
    private javax.swing.JTextField txtUserName;
    // End of variables declaration//GEN-END:variables
}
package com.project.labautomationsystem.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sunny
 */
public class Server extends javax.swing.JFrame {

   ArrayList<PrintWriter> clientOutputStreams;
   ArrayList<String> users;
   String home;
   ServerSocket serverSock;

   public class ClientHandler implements Runnable	
   {
       BufferedReader reader;
       Socket sock;
       PrintWriter client;
       File file;
       DefaultListModel<?> model;
       DefaultListModel<?> fmodel;
       DefaultTableModel tmodel;
       
       

       public ClientHandler(Socket clientSocket, PrintWriter user) 
       {
            client = user;
            try 
            {
                sock = clientSocket;
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);
            }
            catch (Exception ex) 
            {
                txtLog.append("Unexpected error... \n");
            }

       }

        @Override
       public void run() 
       {
            String message;
            String[] data;

            try 
            {
                while ((message = reader.readLine()) != null) 
                {
                    txtLog.append("Received: " + message + "\n");
                    data = message.split("`");
                    
                    for (String token:data) 
                    {
                        txtLog.append(token + "\n");
                    }

                    switch (data[0]) {
                        case "login":
                            tellEveryone(message);
                            userAdd(data[1]);
                            tellEveryone("online`"+data[1]+"`"+onlineUser());
                            txtLog.append(data[1]+" connected\n");
                            tellEveryone("NS`"+getNotes());
                            tellEveryone("DF`"+getDiscussion());
                            tellEveryone("FS`"+getFileList());
                            break;
                        case "disconnect":
                            tellEveryone(message);
                            userRemove(data[1]);
                            String str1 = txtStdPre.getText();
                            str1.replace(data[2]," ");
                            System.out.println(str1);
                            str1.trim();
                            System.out.println(str1);
                            txtStdPre.setText(str1);
                            txtLog.append(data[1]+" disconnected\n");
                            break;
                        case "CH":
                            tellEveryone(message);
                            txtLog.append(message+"\n");
                            break;
                        case "NS":
                            try{
                                FileReader fr=new FileReader(home+"/Documents/GSCExtras/Notes.txt");
                                int i;
                                String str="";
                                while((i=fr.read())!=-1)
                                    str+=""+(char)i;
                                fr.close();
                                FileWriter fout = new FileWriter(home+"/Documents/GSCExtras/Notes.txt");
                                fout.write(str+""+data[1]+"#@#"+data[2]+"#@#"+data[3]+"~");
                                fout.close();
                                tellEveryone("NS`"+getNotes());
                                tellEveryone("DF`"+getDiscussion());
                                tellEveryone("FS`"+getFileList());
                            }catch(Exception e){}
                            break;
                        case "DF":
                            try{
                                FileReader fr=new FileReader(home+"/Documents/GSCExtras/count.txt");
                                int i,count=0;
                                String str="";
                                while((i=fr.read())!=-1)
                                    str+=""+(char)i;
                                fr.close();
                                if(str.equals(""))
                                    count=0;
                                else
                                    count=Integer.parseInt(str);
                                FileWriter fout = new FileWriter(home+"/Documents/GSCExtras/count.txt");
                                fout.write(""+(count+1));
                                fout.close();
                                String file=home+"/Documents/GSCExtras/Q"+(count+1)+".txt";
                                fout = new FileWriter(file);
                                fout.write(data[1]+"#@#"+data[2]+"~");
                                fout.close();
                                tellEveryone("DF`"+getDiscussion());
                            }catch(Exception e){}
                            break;
                        case "DFA":
                            String file,str="";
                            if(data[1].contains("q") || data[1].contains("Q"))
                                file=data[1].toUpperCase()+".txt";
                            else
                                file=home+"/Documents/GSCExtras/Q"+data[1]+".txt";
                            str+=new String(Files.readAllBytes(Paths.get(file)));
                            str=str.substring(0, str.length()-1);
                            txtLog.setText(str);
                            String[] q=str.split("#@#");
                            str="";
                            for(int k=0;k<q.length;k++)
                                str+=q[k]+"#@#";
                            str+=data[2]+"~";
                            FileWriter fout = new FileWriter(file);
                            fout.write(str);
                            fout.close();
                            tellEveryone("DF`"+getDiscussion());
                            break;
                        case "FT":
                            tellEveryone(message);
                            txtLog.append("File transfer request recieved\n");
                            break;
                        case "FS":
                            try{
                            String[] msg = message.split("#@#@#");
                            msg[0]=msg[0].substring(3);
                            byte[] decodedBytes = Base64.getDecoder().decode(msg[1]);
                            String home = System.getProperty("user.home");
                            OutputStream out = new FileOutputStream(home+"/Documents/GSCExtras/FileSharing/"+msg[0]);
                            out.write(decodedBytes);
                            out.close();
                            tellEveryone("FS`"+getFileList());
                            }catch(Exception e){}
                            break;
                        case "FSR":
                            File f1=new File(home+"/Documents/GSCExtras/FileSharing/"+data[2]);
                            byte[] byteArray = new byte[(int) f1.length()];
                            FileInputStream fileInputStream = new FileInputStream(f1);
                            fileInputStream.read(byteArray);
                            String base64String = Base64.getEncoder().encodeToString(byteArray);
                            tellEveryone("FSR`"+data[1]+"`"+data[2]+"`"+base64String);
                            break;
                        case "REFRESH":
                            users.clear();
                            tellEveryone("DisconnectAll` ");
                            break;
                        case "RESET":
                            users.clear();
                            File dir=new File(home+"/Documents/GSCExtras/");
                            for(File f:dir.listFiles())
                                if(!f.isDirectory()|| !f.isFile())
                                    f.delete();
                            tellEveryone("DisconnectAllReset` ");
                            dir=new File(home+"/Documents/GSCExtras/FileSharing");
                            for(File f:dir.listFiles())
                                if(!f.isDirectory() || !f.isFile())
                                    f.delete();
                            tellEveryone("DisconnectAllReset` ");
                            break;
                        case "Attendance":
                            txtStdPre.append(data[1]+"\n");
                            break;   
                       
                        default:
                            txtLog.append("No Conditions were met. \n");
                            break;
                    }
                } 
             } 
             catch (Exception ex) 
             {
                txtLog.append("Lost a connection. \n");
                clientOutputStreams.remove(client);
             } 
	} 
    }
    public Server() {
        initComponents();
        this.setTitle("Lab Automation System - SERVER");
        home = System.getProperty("user.home");
        File f=new File(home+"/Documents/GSCExtras");
        if(!f.exists())
            f.mkdir();
        f=new File(home+"/Documents/GSCExtras/FileSharing");
        if(!f.exists())
            f.mkdir();
    }
    DefaultListModel<?> fmodel;
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        btnStart = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtStdPre = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        rest = new javax.swing.JButton();
        shut = new javax.swing.JButton();
        logo = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();

        jFormattedTextField1.setText("jFormattedTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        btnStart.setText("Start Server");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        jTabbedPane1.setEnabled(false);

        txtLog.setColumns(20);
        txtLog.setRows(5);
        jScrollPane2.setViewportView(txtLog);

        jTabbedPane1.addTab("Status", jScrollPane2);

        txtStdPre.setColumns(20);
        txtStdPre.setRows(5);
        jScrollPane3.setViewportView(txtStdPre);

        jTabbedPane1.addTab("Students Present", jScrollPane3);

        rest.setBackground(new java.awt.Color(153, 153, 255));
        rest.setText("Restart all Clients");
        rest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restActionPerformed(evt);
            }
        });

        shut.setBackground(new java.awt.Color(153, 153, 255));
        shut.setText("Shut down all Clients ");
        shut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shutActionPerformed(evt);
            }
        });

        logo.setBackground(new java.awt.Color(153, 153, 255));
        logo.setText("Sleep all Clients");
        logo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoActionPerformed(evt);
            }
        });

        jButton2.setText("Disable Remote Network");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton4.setText("Enable Remote Network");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(173, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(shut, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(logo, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(rest, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(165, 165, 165))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(jButton4)
                .addGap(18, 18, 18)
                .addComponent(shut)
                .addGap(18, 18, 18)
                .addComponent(rest)
                .addGap(18, 18, 18)
                .addComponent(logo)
                .addContainerGap(66, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Remote Control", jPanel1);

        jButton1.setText("Start WhiteBorad");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(191, 191, 191)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(195, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jButton1)
                .addContainerGap(232, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("WhiteBorad", jPanel2);

        jButton3.setText("Detect Pendrive avilable on Client System");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(162, Short.MAX_VALUE)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(127, 127, 127))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(92, 92, 92)
                .addComponent(jButton3)
                .addContainerGap(167, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Pendrive Dection", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnStart))
                    .addComponent(jTabbedPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(btnStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @SuppressWarnings("deprecation")
	private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        Thread starter = new Thread(new ServerStart());
        starter.start();
        btnStart.setEnabled(false);
         try {
             txtLog.append("Server has been started @ "+Inet4Address.getLocalHost().getHostAddress()+":51017\n");// TODO add your handling code here:
             jTabbedPane1.enable(true);
         } catch (UnknownHostException ex) {
             Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
         }
    }//GEN-LAST:event_btnStartActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    File file = new File("C:\\Users\\sunny\\Documents\\GSCExtras\\FileSharing");      
       String[] myFiles;    
           if(file.isDirectory()){
               myFiles = file.list();
               for (int i=0; i<myFiles.length; i++) {
                   File myFile = new File(file, myFiles[i]); 
                   myFile.delete();
               }
            }
    File file1 = new File("C:\\Users\\sunny\\Documents\\GSCExtras");      
       String[] myFiles1;    
           if(file1.isDirectory()){
               myFiles1 = file1.list();
               for (int i=0; i<myFiles1.length; i++) {
                   File myFile = new File(file1, myFiles1[i]); 
                   myFile.delete();
               }
            }        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosing

    public void h(String o)
    {
        
        JOptionPane.showMessageDialog(null,"Pendrive on :"+o+" is detected and plugged in");
        
    }
    public void hh(String o)
    {  
         
        txtLog.append("<--"+"Pendrive on :"+o+" is detected and plugged out"+"\n");
        JOptionPane.showMessageDialog(null,"Pendrive on :"+o+" is detected and plugged out");
        
    }
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            URI uri= new URI("https://vrobbi-nodedrawing.herokuapp.com/#faculty");
            java.awt.Desktop.getDesktop().browse(uri);

            tellEveryone("StartWhite`");

        } catch (Exception e) {
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void logoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoActionPerformed
        // TODO add your handling code here:
        users.clear();
        tellEveryone("LogOff` ");
    }//GEN-LAST:event_logoActionPerformed

    private void shutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shutActionPerformed
        // TODO add your handling code here:
        users.clear();
        tellEveryone("ShutDown` ");
    }//GEN-LAST:event_shutActionPerformed

    private void restActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restActionPerformed
        // TODO add your handling code here:
        users.clear();
        tellEveryone("Restart` ");
    }//GEN-LAST:event_restActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         tellEveryone("Disable` ");
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
         tellEveryone("Enable` ");
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        tellEveryone("Drive`");
        JOptionPane.showMessageDialog(null,"USB Detection module started....");
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed
    
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
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Server().setVisible(true);
            }
        });
    }
    
    public class ServerStart implements Runnable 
    {
        @Override
        public void run() 
        {
            clientOutputStreams = new ArrayList<PrintWriter>();
            users = new ArrayList<String>();  

            try 
            {
                serverSock = new ServerSocket(51017);

                while (true) 
                {
				Socket clientSock = serverSock.accept();
				PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
				clientOutputStreams.add(writer);

				Thread listener = new Thread(new ClientHandler(clientSock, writer));
				listener.start();
				txtLog.append("Got a connection. \n");
                }
            }
            catch (IOException ex)
            {
                txtLog.append("Error making a connection. \n");
            }
        }
    }
    
    public void userAdd (String data) 
    {
        users.add(data);
        String[] tempList = new String[(users.size())];
        users.toArray(tempList);
    }
 
    public void userRemove (String data) 
    {
        users.remove(data);
        String[] tempList = new String[(users.size())];
        users.toArray(tempList);
    }
    
    public void tellEveryone(String message) 
    {
	Iterator<PrintWriter> it = clientOutputStreams.iterator();
        System.out.println("Pendrive tell every one"+"** "+message);
        while (it.hasNext()) 
        {
            try 
            {
                PrintWriter writer = (PrintWriter) it.next();
		writer.println(message);
		txtLog.append("Sending: " + message + "\n");
                writer.flush();
                txtLog.setCaretPosition(txtLog.getDocument().getLength());

            } 
            catch (Exception ex) 
            {
		txtLog.append("Error telling everyone. \n");
            }
        } 
    }
    
    String onlineUser()
    {   String str="";
        for (String current_user : users)
        {   str=str+""+current_user+",";
        }
        return str;
    }
    
    String getNotes() throws IOException
    {
        File f=new File(home+"/Documents/GSCExtras/Notes.txt");
        if(!f.exists())
        {
            FileWriter fout = new FileWriter(home+"/Documents/GSCExtras/Notes.txt");
            fout.write("");
            fout.close();
        }
        FileReader fr=null;
        String str="";
         try {
             fr = new FileReader(home+"/Documents/GSCExtras/Notes.txt");
             int i;
             
             while((i=fr.read())!=-1)  
                 str+=""+(char)i;
             fr.close();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 fr.close();
             } catch (IOException ex) {
                 Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         if(str.equals(""))
         {    str=" #@# #@# ~";
         }
         return str;
    }
    
    String getDiscussion() throws IOException
    {   File f=new File(home+"/Documents/GSCExtras/count.txt");
        if(!f.exists())
        {
            FileWriter fout = new FileWriter(home+"/Documents/GSCExtras/count.txt");
            fout.write("0");
            fout.close();
        }
        FileReader fr=new FileReader(home+"/Documents/GSCExtras/count.txt");
        int i,count=0;  
        String str="";
        while((i=fr.read())!=-1)  
        str+=""+(char)i;
        fr.close();  
        if(str.equals(""))
            count=0;
        else
            count=Integer.parseInt(str);
        String file="";
        str="";
        for(i=1;i<=count;i++)
        {    file=home+"/Documents/GSCExtras/Q"+i+".txt";
             str+=new String(Files.readAllBytes(Paths.get(file)));  
        }  
        if(str.equals(""))
        {    str=" #@# ~";
        }
        return str;
    }
    
    String getFileList()
    {
        String str="";
        File[] files = new File(home+"/Documents/GSCExtras/FileSharing").listFiles();
        for (File file : files){
        if (file.isFile()) {
            str+=file.getName()+"#@#";
            }
        }
        if(str.equals(""))
            str=" #@# ";
        return str;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnStart;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton logo;
    private javax.swing.JButton rest;
    private javax.swing.JButton shut;
    private javax.swing.JTextArea txtLog;
    private javax.swing.JTextArea txtStdPre;
    // End of variables declaration//GEN-END:variables
}

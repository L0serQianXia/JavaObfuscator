package me.qianxia.obfuscator;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import me.qianxia.obfuscator.config.Config;
import me.qianxia.obfuscator.utils.LogUtils;

/**
 * @description: 图形界面
 * @author: QianXia
 * @create: 2021-02-25 15:16
 **/
public class GUI extends JFrame {
    private static final long serialVersionUID = 2577805627250426986L;

    private JPanel contentPane;
    private JTextField inputTextField;
    private JTextField outputTextField;
    private JTextField classPathTextField;
    private JTextField exclude;
    private JavaObfuscator obf = new JavaObfuscator();
    private String using = "";

    public GUI() {
        setTitle(JavaObfuscator.NAME + " V" + JavaObfuscator.VERSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 332, 283);
        setResizable(false);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        int height = 23;
        int temp = 0;
        String[] obfuscators = JavaObfuscator.OBFUSCATORS.split("-");
        String[] describes = JavaObfuscator.DESCRIBES.split("-");
        for (String obf : obfuscators) {
            JCheckBox checkBox = new JCheckBox(describes[temp++] + "混淆");
            checkBox.setBounds(37, height, 120, 23);
            checkBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (((JCheckBox) e.getItem()).isSelected()) {
                        using += obf;
                    } else {
                        using = using.replace(obf, "");
                    }
                }
            });
            contentPane.add(checkBox);
            height += 30;
        }

        if (height < 100) {
            height += 100;
        }
        setBounds(500, 500, 340, height + 120);

        JButton btnNewButton = new JButton("混淆");
        btnNewButton.addActionListener(arg0 -> {
            JTextArea area = new JTextArea();
            area.setFont(new Font("微软雅黑", 0, 14));
            LogUtils.isGUI = true;
            LogUtils.GUIText = area;
            btnNewButton.setEnabled(false);
            JFrame newFrame = new JFrame();
            newFrame.setTitle("控制台");
            area.setEditable(false);
            newFrame.getContentPane().add(new JScrollPane(area));
            // newFrame.pack();
            newFrame.setSize(800, 600);
            newFrame.setVisible(true);
            new Thread(() -> {
                if (using.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "您应该至少选择一个混淆项", "错误：", JOptionPane.ERROR_MESSAGE, null);
                    return;
                }
                if (inputTextField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "您应该添加一个输入文件", "错误：", JOptionPane.ERROR_MESSAGE, null);
                    return;
                }
                Config conf = new Config(inputTextField.getText(), outputTextField.getText(),
                        classPathTextField.getText(), using, exclude.getText().split(" "));
                Main.runObfuscator(conf);
            }, "Obfuscator thread").start();

            newFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    area.setText(null);

                    btnNewButton.setEnabled(true);
                    obf.clearClasses();
                    e.getWindow().dispose();
                }
            });
        });

        btnNewButton.setBounds(37, getHeight() - 100, 93, 23);
        contentPane.add(btnNewButton);

        JLabel lblNewLabel = new JLabel("Code By QianXia");
        lblNewLabel.setBounds(111, getHeight() - 55, 100, 15);
        contentPane.add(lblNewLabel);

        inputTextField = new JTextField();
        inputTextField.setBounds(202, 24, 104, 21);
        contentPane.add(inputTextField);
        inputTextField.setColumns(10);

        JLabel lblNewLabel_2 = new JLabel("输入:");
        lblNewLabel_2.setBounds(162, 27, 30, 15);
        contentPane.add(lblNewLabel_2);

        JButton btnNewButton_1 = new JButton("选择文件");
        btnNewButton_1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().endsWith(".jar");
                    }

                    @Override
                    public String getDescription() {
                        return "*.jar";
                    }
                });
                chooser.showDialog(new JLabel(), "选择文件");
                File file = chooser.getSelectedFile();
                if (file == null) {
                    return;
                }
                inputTextField.setText(file.getAbsolutePath());
                obf.setInput(file.getAbsolutePath());
            }
        });
        btnNewButton_1.setBounds(205, 49, 93, 23);
        contentPane.add(btnNewButton_1);

        outputTextField = new JTextField();
        outputTextField.setColumns(10);
        outputTextField.setBounds(202, 78, 104, 21);
        contentPane.add(outputTextField);

        JLabel lblNewLabel_2_1 = new JLabel("输出:");
        lblNewLabel_2_1.setBounds(162, 80, 30, 15);
        contentPane.add(lblNewLabel_2_1);

        JButton btnNewButton_1_1 = new JButton("选择文件");
        btnNewButton_1_1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().endsWith(".jar");
                    }

                    @Override
                    public String getDescription() {
                        return "*.jar";
                    }
                });
                chooser.showDialog(new JLabel(), "选择文件");
                File file = chooser.getSelectedFile();
                if (file == null) {
                    return;
                }
                inputTextField.setText(file.getAbsolutePath());
                obf.setInput(file.getAbsolutePath());
            }
        });
        btnNewButton_1_1.setBounds(205, 102, 93, 23);
        contentPane.add(btnNewButton_1_1);

        exclude = new JTextField();
        exclude.setColumns(10);
        exclude.setBounds(202, 185, 104, 21);
        contentPane.add(exclude);

        JLabel excludeLabel = new JLabel("排除类:");
        excludeLabel.setBounds(160, 189, 49, 15);
        contentPane.add(excludeLabel);

        classPathTextField = new JTextField();
        classPathTextField.setColumns(10);
        classPathTextField.setBounds(202, 131, 104, 21);
        contentPane.add(classPathTextField);

        JLabel lblNewLabel_2_1_1 = new JLabel("支持库:");
        lblNewLabel_2_1_1.setBounds(160, 134, 49, 15);
        contentPane.add(lblNewLabel_2_1_1);

        JButton selectClasspathDirButton = new JButton("选择目录");
        selectClasspathDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.showDialog(new JLabel(), "选择目录");
                File file = chooser.getSelectedFile();
                if (file == null) {
                    return;
                }
                classPathTextField.setText(file.getAbsolutePath());
                obf.setClasspath(file.getAbsolutePath());
            }
        });
        selectClasspathDirButton.setBounds(205, 156, 93, 23);
        contentPane.add(selectClasspathDirButton);
    }

}

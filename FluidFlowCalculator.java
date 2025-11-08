import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

/**
 * 流体力学长管第二类问题求解程序 - Java简单版本
 * 
 * 本程序实现了流体力学中简单长管第二类问题的求解，
 * 已知管道参数和总水头损失，通过流量试算法计算流量和流态。
 */
public class FluidFlowCalculator extends JFrame {
    
    // 界面组件
    private JTextField txtPipeLength;      // 管道长度输入框
    private JTextField txtPipeDiameter;    // 管道直径输入框
    private JTextField txtRoughness;       // 绝对粗糙度输入框
    private JTextField txtLocalLoss;       // 局部阻力系数输入框
    private JTextField txtFluidDensity;    // 流体密度输入框
    private JTextField txtFluidViscosity;  // 流体粘度输入框
    private JTextField txtHeadLoss;        // 总水头损失输入框
    private JTextArea txtResult;           // 结果显示区域
    
    // 常量定义
    private static final double CRITICAL_REYNOLDS = 2000.0;  // 临界雷诺数
    private static final double GRAVITY = 9.8;               // 重力加速度
    private static final double TOLERANCE = 1e-6;            // 计算精度
    private static final int MAX_ITERATIONS = 100;           // 最大迭代次数
    
    /**
     * 构造方法，初始化GUI界面
     */
    public FluidFlowCalculator() {
        setTitle("流体力学长管第二类问题求解器");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 窗口居中
        
        // 设置界面布局
        initializeUI();
    }
    
    /**
     * 初始化用户界面组件
     */
    private void initializeUI() {
        // 创建主面板
        CustomPanel mainPanel = new CustomPanel(new BorderLayout());
        mainPanel.setPadding(15);
        
        // 创建输入面板（左侧）
        CustomPanel inputPanel = new CustomPanel(new GridLayout(8, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("输入参数"));
        
        // 添加管道参数输入
        inputPanel.add(new JLabel("管道长度 (m):"));
        txtPipeLength = new JTextField("100.0");
        inputPanel.add(txtPipeLength);
        
        inputPanel.add(new JLabel("管道直径 (m):"));
        txtPipeDiameter = new JTextField("0.1");
        inputPanel.add(txtPipeDiameter);
        
        inputPanel.add(new JLabel("绝对粗糙度 (m):"));
        txtRoughness = new JTextField("0.00015");
        inputPanel.add(txtRoughness);
        
        inputPanel.add(new JLabel("局部阻力系数:"));
        txtLocalLoss = new JTextField("0.0");
        inputPanel.add(txtLocalLoss);
        
        // 添加流体参数输入
        inputPanel.add(new JLabel("流体密度 (kg/m³):"));
        txtFluidDensity = new JTextField("998.2");
        inputPanel.add(txtFluidDensity);
        
        inputPanel.add(new JLabel("动力粘度 (Pa·s):"));
        txtFluidViscosity = new JTextField("1.003e-3");
        inputPanel.add(txtFluidViscosity);
        
        // 添加水头损失输入
        inputPanel.add(new JLabel("总水头损失 (m):"));
        txtHeadLoss = new JTextField("10.0");
        inputPanel.add(txtHeadLoss);
        
        // 创建计算按钮
        JButton btnCalculate = new JButton("计算流量");
        btnCalculate.addActionListener(new CalculateButtonListener());
        
        // 创建重置按钮
        JButton btnReset = new JButton("重置");
        btnReset.addActionListener(new ResetButtonListener());
        
        // 按钮面板
        CustomPanel buttonPanel = new CustomPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(btnCalculate);
        buttonPanel.add(btnReset);
        
        // 创建结果面板
        CustomPanel resultPanel = new CustomPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("计算结果"));
        
        txtResult = new JTextArea(10, 40);
        txtResult.setEditable(false);
        txtResult.setLineWrap(true);
        txtResult.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(txtResult);
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 组装主面板
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(resultPanel, BorderLayout.SOUTH);
        
        // 添加主面板到窗口
        add(mainPanel);
    }
    
    /**
     * 计算按钮监听器类
     */
    private class CalculateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // 读取输入参数
                double pipeLength = Double.parseDouble(txtPipeLength.getText());
                double pipeDiameter = Double.parseDouble(txtPipeDiameter.getText());
                double roughness = Double.parseDouble(txtRoughness.getText());
                double localLossCoeff = Double.parseDouble(txtLocalLoss.getText());
                double fluidDensity = Double.parseDouble(txtFluidDensity.getText());
                double fluidViscosity = parseScientificNotation(txtFluidViscosity.getText());
                double totalHeadLoss = Double.parseDouble(txtHeadLoss.getText());
                
                // 参数验证
                if (pipeLength <= 0 || pipeDiameter <= 0 || roughness < 0 || 
                    localLossCoeff < 0 || fluidDensity <= 0 || fluidViscosity <= 0 || 
                    totalHeadLoss <= 0) {
                    JOptionPane.showMessageDialog(FluidFlowCalculator.this, 
                            "输入参数必须为正数！", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // 执行计算
                FlowResult result = calculateFlowRate(pipeLength, pipeDiameter, roughness, 
                                                    localLossCoeff, fluidDensity, 
                                                    fluidViscosity, totalHeadLoss);
                
                // 显示结果
                displayResults(result);
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(FluidFlowCalculator.this, 
                        "请输入有效的数值！", "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(FluidFlowCalculator.this, 
                        "计算过程中出错：" + ex.getMessage(), "计算错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 重置按钮监听器类
     */
    private class ResetButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 重置输入框
            txtPipeLength.setText("100.0");
            txtPipeDiameter.setText("0.1");
            txtRoughness.setText("0.00015");
            txtLocalLoss.setText("0.0");
            txtFluidDensity.setText("998.2");
            txtFluidViscosity.setText("1.003e-3");
            txtHeadLoss.setText("10.0");
            
            // 清空结果
            txtResult.setText("");
        }
    }
    
    /**
     * 解析科学计数法格式的字符串为双精度浮点数
     * 
     * @param text 科学计数法格式的字符串
     * @return 解析后的双精度浮点数
     */
    private double parseScientificNotation(String text) {
        text = text.trim();
        // 简单检查是否为科学计数法
        if (text.contains("e") || text.contains("E")) {
            return Double.parseDouble(text);
        }
        // 否则尝试直接转换
        return Double.parseDouble(text);
    }
    
    /**
     * 流量试算法计算管道流量
     * 
     * @param pipeLength 管道长度 (m)
     * @param pipeDiameter 管道直径 (m)
     * @param roughness 绝对粗糙度 (m)
     * @param localLossCoeff 局部阻力系数
     * @param fluidDensity 流体密度 (kg/m³)
     * @param fluidViscosity 流体粘度 (Pa·s)
     * @param totalHeadLoss 总水头损失 (m)
     * @return 计算结果对象
     */
    private FlowResult calculateFlowRate(double pipeLength, double pipeDiameter, double roughness, 
                                       double localLossCoeff, double fluidDensity, 
                                       double fluidViscosity, double totalHeadLoss) {
        
        // 初始假设流量（基于层流公式）
        double initialFlow = (Math.PI * Math.pow(pipeDiameter, 4) * fluidDensity * GRAVITY * totalHeadLoss) /
                           (128 * fluidViscosity * pipeLength);
        
        double flowRate = initialFlow;
        int iterationCount = 0;
        
        // 迭代求解
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            iterationCount = i + 1;
            
            // 计算雷诺数
            double reynoldsNumber = calculateReynoldsNumber(flowRate, pipeDiameter, fluidDensity, fluidViscosity);
            
            // 计算沿程阻力系数
            double frictionFactor = calculateFrictionFactor(reynoldsNumber, roughness, pipeDiameter);
            
            // 计算总阻力系数
            double totalFrictionFactor = frictionFactor * (pipeLength / pipeDiameter) + localLossCoeff;
            
            // 计算新的流量
            double newFlowRate = Math.sqrt((2 * GRAVITY * totalHeadLoss * Math.PI * Math.PI * Math.pow(pipeDiameter, 5)) /
                                         (totalFrictionFactor * fluidDensity * 8));
            
            // 检查收敛
            if (Math.abs(newFlowRate - flowRate) / flowRate < TOLERANCE) {
                break;
            }
            
            flowRate = newFlowRate;
        }
        
        // 计算最终结果
        double reynoldsNumber = calculateReynoldsNumber(flowRate, pipeDiameter, fluidDensity, fluidViscosity);
        double frictionFactor = calculateFrictionFactor(reynoldsNumber, roughness, pipeDiameter);
        double velocity = (4 * flowRate) / (Math.PI * Math.pow(pipeDiameter, 2));
        
        // 判断流态
        String flowType = (reynoldsNumber <= CRITICAL_REYNOLDS) ? "层流" : "湍流";
        
        // 计算水头损失
        double headLossFriction = frictionFactor * (pipeLength / pipeDiameter) * (velocity * velocity) / (2 * GRAVITY);
        double headLossLocal = localLossCoeff * (velocity * velocity) / (2 * GRAVITY);
        
        // 返回计算结果
        return new FlowResult(flowRate, velocity, reynoldsNumber, frictionFactor, flowType, 
                            headLossFriction, headLossLocal, iterationCount);
    }
    
    /**
     * 计算雷诺数
     * 
     * @param flowRate 流量 (m³/s)
     * @param diameter 管道直径 (m)
     * @param density 流体密度 (kg/m³)
     * @param viscosity 流体粘度 (Pa·s)
     * @return 雷诺数
     */
    private double calculateReynoldsNumber(double flowRate, double diameter, double density, double viscosity) {
        double velocity = (4 * flowRate) / (Math.PI * Math.pow(diameter, 2));
        return (density * velocity * diameter) / viscosity;
    }
    
    /**
     * 计算沿程阻力系数
     * 
     * @param reynoldsNumber 雷诺数
     * @param roughness 绝对粗糙度 (m)
     * @param diameter 管道直径 (m)
     * @return 沿程阻力系数
     */
    private double calculateFrictionFactor(double reynoldsNumber, double roughness, double diameter) {
        if (reynoldsNumber <= CRITICAL_REYNOLDS) {
            // 层流状态：泊肃叶公式
            return 64.0 / reynoldsNumber;
        } else {
            // 湍流状态：使用科尔布鲁克公式迭代求解
            return solveColebrookFormula(reynoldsNumber, roughness, diameter);
        }
    }
    
    /**
     * 使用科尔布鲁克公式求解湍流状态下的沿程阻力系数
     * 
     * @param reynoldsNumber 雷诺数
     * @param roughness 绝对粗糙度 (m)
     * @param diameter 管道直径 (m)
     * @return 沿程阻力系数
     */
    private double solveColebrookFormula(double reynoldsNumber, double roughness, double diameter) {
        double f_old = 0.02;  // 初始猜测值
        
        for (int i = 0; i < 100; i++) {  // 迭代求解
            double relativeRoughness = roughness / diameter;
            double term1 = relativeRoughness / 3.7;
            double term2 = 2.51 / (reynoldsNumber * Math.sqrt(f_old));
            double f_new = 1.0 / Math.pow(-2.0 * Math.log10(term1 + term2), 2);
            
            if (Math.abs(f_new - f_old) < TOLERANCE) {
                return f_new;
            }
            
            f_old = f_new;
        }
        
        return f_old;  // 返回最后一次迭代的值
    }
    
    /**
     * 显示计算结果
     * 
     * @param result 计算结果对象
     */
    private void displayResults(FlowResult result) {
        StringBuilder sb = new StringBuilder();
        DecimalFormat df6 = new DecimalFormat("0.000000");
        DecimalFormat df4 = new DecimalFormat("0.0000");
        DecimalFormat df2 = new DecimalFormat("0.00");
        
        sb.append("流体力学长管第二类问题计算结果\n");
        sb.append("=======================================\n\n");
        
        sb.append("计算结果:\n");
        sb.append("- 流量: " + df4.format(result.flowRate * 1000) + " L/s  (" + df6.format(result.flowRate) + " m³/s)\n");
        sb.append("- 流速: " + df4.format(result.velocity) + " m/s\n");
        sb.append("- 雷诺数: " + df2.format(result.reynoldsNumber) + "\n");
        sb.append("- 流态: " + result.flowType + "\n");
        sb.append("- 沿程阻力系数: " + df6.format(result.frictionFactor) + "\n");
        sb.append("- 沿程水头损失: " + df4.format(result.headLossFriction) + " m\n");
        sb.append("- 局部水头损失: " + df4.format(result.headLossLocal) + " m\n");
        sb.append("- 迭代次数: " + result.iterations + " 次\n\n");
        
        sb.append("计算说明:\n");
        sb.append("1. 使用流量试算法求解长管第二类问题\n");
        sb.append("2. 初始假设流量基于层流公式\n");
        sb.append("3. 层流状态(Re ≤ " + CRITICAL_REYNOLDS + "): f = 64/Re\n");
        sb.append("4. 湍流状态(Re > " + CRITICAL_REYNOLDS + "): 使用科尔布鲁克公式\n");
        
        txtResult.setText(sb.toString());
    }
    
    /**
     * 计算结果内部类，用于存储计算结果
     */
    private class FlowResult {
        double flowRate;           // 流量 (m³/s)
        double velocity;           // 流速 (m/s)
        double reynoldsNumber;     // 雷诺数
        double frictionFactor;     // 沿程阻力系数
        String flowType;           // 流态（层流/湍流）
        double headLossFriction;   // 沿程水头损失 (m)
        double headLossLocal;      // 局部水头损失 (m)
        int iterations;            // 迭代次数
        
        /**
         * 构造方法
         */
        public FlowResult(double flowRate, double velocity, double reynoldsNumber, 
                         double frictionFactor, String flowType, double headLossFriction, 
                         double headLossLocal, int iterations) {
            this.flowRate = flowRate;
            this.velocity = velocity;
            this.reynoldsNumber = reynoldsNumber;
            this.frictionFactor = frictionFactor;
            this.flowType = flowType;
            this.headLossFriction = headLossFriction;
            this.headLossLocal = headLossLocal;
            this.iterations = iterations;
        }
    }
    
    /**
     * 主方法，程序入口
     */
    public static void main(String[] args) {
        // 在事件调度线程中运行GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                FluidFlowCalculator calculator = new FluidFlowCalculator();
                calculator.setVisible(true);
            }
        });
    }
    
    /**
     * 扩展JPanel类，添加设置内边距的方法
     */
    private static class CustomPanel extends javax.swing.JPanel {
        public CustomPanel(LayoutManager layout) {
            super(layout);
        }
        
        public CustomPanel() {
            super();
        }
        
        public void setPadding(int padding) {
            setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        }
    }
}
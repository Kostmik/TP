import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class RailwaySystem extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    private Administrator admin = new Administrator();
    private CargoManager cargoManager = new CargoManager(); // Для управления грузом
    private UserManager userManager = new UserManager(); // Для управления пользователями
    private OperatorManager operatorManager = new OperatorManager(); // Для управления операторами

    public RailwaySystem() {
        // Настройка окна авторизации
        setTitle("Авторизация в системе");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 2));
        setLocationRelativeTo(null); // Открытие окна в центре экрана

        // Поля для ввода данных
        JLabel usernameLabel = new JLabel("Логин:");
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Пароль:");
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Войти");
        JButton registerButton = new JButton("Зарегистрироваться");

        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(new JLabel()); // Пустая ячейка
        add(loginButton);
        add(new JLabel()); // Пустая ячейка для выравнивания
        add(registerButton);

        // Обработчик события для кнопки "Войти"
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                authenticateUser(username, password);
            }
        });

        // Обработчик для кнопки "Зарегистрироваться"
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegistrationForm();
            }
        });
    }

    // Метод для проверки авторизации
    private void authenticateUser(String username, String password) {
        if (username.equals("admin") && password.equals("admin123")) {
            JOptionPane.showMessageDialog(this, "Добро пожаловать, Администратор!");
            new AdminWindow().setVisible(true);
        } else if (userManager.isValidUser(username, password)) {
            JOptionPane.showMessageDialog(this, "Добро пожаловать, Пользователь!");
            new UserWindow().setVisible(true);
        } else if (operatorManager.isValidOperator(username, password)) {
            JOptionPane.showMessageDialog(this, "Добро пожаловать, Оператор " + username + "!");
            String stationName = operatorManager.getStationForOperator(username);  // Получаем станцию оператора
            new OperatorWindow(username, stationName).setVisible(true);  // Передаем станцию в панель оператора
        } else {
            JOptionPane.showMessageDialog(this, "Неверный логин или пароль!", "Ошибка авторизации", JOptionPane.ERROR_MESSAGE);
        }
        dispose();
    }

    // Метод для открытия формы регистрации
    private void openRegistrationForm() {
        JFrame registrationFrame = new JFrame("Регистрация нового пользователя");
        registrationFrame.setSize(400, 300);
        registrationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        registrationFrame.setLayout(new GridLayout(4, 2));
        registrationFrame.setLocationRelativeTo(null); // Открытие окна в центре экрана

        // Поля для ввода данных
        JLabel newUsernameLabel = new JLabel("Логин:");
        JTextField newUsernameField = new JTextField();
        JLabel newPasswordLabel = new JLabel("Пароль:");
        JPasswordField newPasswordField = new JPasswordField();

        JButton registerUserButton = new JButton("Зарегистрироваться");

        registrationFrame.add(newUsernameLabel);
        registrationFrame.add(newUsernameField);
        registrationFrame.add(newPasswordLabel);
        registrationFrame.add(newPasswordField);
        registrationFrame.add(new JLabel()); // Пустая ячейка
        registrationFrame.add(registerUserButton);

        // Обработчик регистрации
        registerUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newUsername = newUsernameField.getText();
                String newPassword = new String(newPasswordField.getPassword());

                if (newUsername.isEmpty() || newPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(registrationFrame, "Логин и пароль не могут быть пустыми!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (userManager.userExists(newUsername)) {
                    JOptionPane.showMessageDialog(registrationFrame, "Пользователь с таким логином уже существует!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                } else {
                    userManager.registerUser(newUsername, newPassword);
                    JOptionPane.showMessageDialog(registrationFrame, "Регистрация успешна!");
                    registrationFrame.dispose();
                }
            }
        });

        registrationFrame.setVisible(true);
    }

    // Окно для администратора
    class AdminWindow extends JFrame {
        private DefaultListModel<String> stationsModel;
        private DefaultListModel<String> operatorsModel;
        private DefaultListModel<String> trainsModel;

        public AdminWindow() {
            setTitle("Администратор");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new GridLayout(1, 3)); // три колонки для списков
            setLocationRelativeTo(null); // Открытие окна в центре экрана

            // Инициализация моделей списков
            stationsModel = new DefaultListModel<>();
            operatorsModel = new DefaultListModel<>();
            trainsModel = new DefaultListModel<>();

            // Создание панелей с использованием BoxLayout для вертикального размещения
            JPanel stationPanel = new JPanel();
            stationPanel.setLayout(new BoxLayout(stationPanel, BoxLayout.Y_AXIS));
            JPanel operatorPanel = new JPanel();
            operatorPanel.setLayout(new BoxLayout(operatorPanel, BoxLayout.Y_AXIS));
            JPanel trainPanel = new JPanel();
            trainPanel.setLayout(new BoxLayout(trainPanel, BoxLayout.Y_AXIS));

            // Загрузка данных из файлов при запуске
            admin.loadFromFile("stations.txt", stationsModel);
            admin.loadFromFile("operators.txt", operatorsModel);
            admin.loadFromFile("trains.txt", trainsModel);

            // Создание панели с кнопками удаления
            JScrollPane stationScrollPane = createListWithDeleteButtons(stationsModel, "stations.txt", true);
            JScrollPane operatorScrollPane = createListWithDeleteButtons(operatorsModel, "operators.txt", false);
            JScrollPane trainScrollPane = createListWithDeleteButtons(trainsModel, "trains.txt", false);

            // Кнопки под каждым списком
            JButton addStationButton = new JButton("Добавить станцию");
            JButton addOperatorButton = new JButton("Добавить оператора");
            JButton addTrainButton = new JButton("Добавить поезд");

            // Уменьшаем размер кнопок
            Dimension buttonSize = new Dimension(150, 30);
            addStationButton.setMaximumSize(buttonSize);
            addOperatorButton.setMaximumSize(buttonSize);
            addTrainButton.setMaximumSize(buttonSize);

            // Добавляем списки и кнопки в соответствующие панели
            stationPanel.add(stationScrollPane);
            stationPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Отступ
            stationPanel.add(addStationButton);
            operatorPanel.add(operatorScrollPane);
            operatorPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Отступ
            operatorPanel.add(addOperatorButton);
            trainPanel.add(trainScrollPane);
            trainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Отступ
            trainPanel.add(addTrainButton);

            // Добавляем панели в окно
            add(stationPanel);
            add(operatorPanel);
            add(trainPanel);

            // Обработчики событий для кнопок
            addStationButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String stationName = JOptionPane.showInputDialog("Введите название станции:");
                    if (stationName != null && !stationName.trim().isEmpty()) {
                        admin.addStation(stationName);
                        JOptionPane.showMessageDialog(null, "Станция " + stationName + " добавлена!");
                        reloadWindow(); // Перезагрузка окна после добавления
                    }
                }
            });

            addOperatorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String stationName = JOptionPane.showInputDialog("Введите название станции:");
                    if (stationName == null || stationName.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Название станции не может быть пустым!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String operatorName = JOptionPane.showInputDialog("Введите имя оператора:");
                    if (operatorName == null || operatorName.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Имя оператора не может быть пустым!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (admin.stationExists(stationName)) {
                        admin.addOperatorToStation(stationName, operatorName);
                        operatorManager.registerOperator(operatorName, stationName); // Регистрация оператора
                        JOptionPane.showMessageDialog(null, "Оператор " + operatorName + " добавлен на станцию " + stationName);
                        reloadWindow(); // Перезагрузка окна после добавления
                    } else {
                        JOptionPane.showMessageDialog(null, "Станция " + stationName + " не найдена!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            addTrainButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String trainNumber = JOptionPane.showInputDialog("Введите номер поезда:");
                    if (trainNumber == null || trainNumber.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Номер поезда не может быть пустым!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String departure = JOptionPane.showInputDialog("Введите станцию отправления:");
                    if (departure == null || departure.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Станция отправления не может быть пустой!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String destination = JOptionPane.showInputDialog("Введите станцию назначения:");
                    if (destination == null || destination.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Станция назначения не может быть пустой!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (admin.stationExists(departure) && admin.stationExists(destination)) {
                        admin.addTrain(trainNumber, departure, destination);
                        JOptionPane.showMessageDialog(null, "Поезд " + trainNumber + " добавлен от " + departure + " до " + destination + "!");
                        reloadWindow(); // Перезагрузка окна после добавления
                    } else {
                        JOptionPane.showMessageDialog(null, "Станция отправления или назначения не найдена!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }

        /**
         * Метод для создания списка с кнопками удаления
         */
        private JScrollPane createListWithDeleteButtons(DefaultListModel<String> model, String fileName, boolean isStation) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            for (int i = 0; i < model.size(); i++) {
                String item = model.get(i);
                JPanel itemPanel = new JPanel();
                itemPanel.setLayout(new BorderLayout());

                JLabel label = new JLabel(item);
                JButton deleteButton = new JButton("X");
                deleteButton.setForeground(Color.RED);
                deleteButton.setMargin(new Insets(2, 5, 2, 5));

                int index = i;
                deleteButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int confirm = JOptionPane.showConfirmDialog(null, "Вы уверены, что хотите удалить \"" + item + "\"?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            model.remove(index);
                            admin.removeFromFile(fileName, item);
                            reloadWindow(); // Перезагрузка окна после удаления
                        }
                    }
                });

                itemPanel.add(label, BorderLayout.CENTER);
                itemPanel.add(deleteButton, BorderLayout.EAST);
                panel.add(itemPanel);
            }

            return new JScrollPane(panel);
        }

        /**
         * Метод для перезагрузки окна администратора
         */
        private void reloadWindow() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    AdminWindow.this.dispose(); // Закрываем текущее окно
                    new AdminWindow().setVisible(true); // Открываем новое окно
                }
            });
        }
    }

    // Окно для оператора
    class OperatorWindow extends JFrame {
        private DefaultListModel<String> cargoListModel;
        private String operatorName;
        private String stationName;

        public OperatorWindow(String operatorName, String stationName) {
            this.operatorName = operatorName;
            this.stationName = stationName;

            setTitle("Панель оператора " + operatorName + " (Станция: " + stationName + ")");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());
            setLocationRelativeTo(null);

            JLabel stationLabel = new JLabel("Станция: " + stationName);

            cargoListModel = new DefaultListModel<>();
            JList<String> cargoList = new JList<>(cargoListModel);
            JScrollPane cargoScrollPane = new JScrollPane(cargoList);

            JButton acceptCargoButton = new JButton("Принять груз");
            JButton dispatchCargoButton = new JButton("Отправить груз");

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(acceptCargoButton);
            buttonPanel.add(dispatchCargoButton);

            add(stationLabel, BorderLayout.NORTH);
            add(cargoScrollPane, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            // Принятие груза от пользователя
            acceptCargoButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String cargo = cargoManager.getCargoForStation(stationName);
                    if (cargo != null) {
                        cargoListModel.addElement(cargo);
                        JOptionPane.showMessageDialog(null, "Груз принят на станцию " + stationName);
                    } else {
                        JOptionPane.showMessageDialog(null, "Нет грузов для приёма на этой станции.", "Информация", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });

            // Отправка груза
            dispatchCargoButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!cargoListModel.isEmpty()) {
                        String cargo = cargoListModel.remove(0);
                        cargoManager.dispatchCargo(cargo);
                        JOptionPane.showMessageDialog(null, "Груз отправлен.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Нет грузов для отправки.", "Информация", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });
        }
    }

    // Окно для пользователя
    class UserWindow extends JFrame {
        private JComboBox<String> fromStationComboBox;
        private JComboBox<String> toStationComboBox;
        private DefaultListModel<String> cargoStatusModel;

        public UserWindow() {
            setTitle("Панель пользователя");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());
            setLocationRelativeTo(null);

            JPanel cargoPanel = new JPanel();
            cargoPanel.setLayout(new GridLayout(3, 2));

            JLabel fromStationLabel = new JLabel("Отправление:");
            fromStationComboBox = new JComboBox<>();
            JLabel toStationLabel = new JLabel("Назначение:");
            toStationComboBox = new JComboBox<>();

            // Загрузка станций из файла stations.txt
            admin.loadStations();
            admin.loadStationsIntoComboBox(fromStationComboBox);
            admin.loadStationsIntoComboBox(toStationComboBox);

            JButton sendCargoButton = new JButton("Отправить груз");

            // Добавляем элементы в панель
            cargoPanel.add(fromStationLabel);
            cargoPanel.add(fromStationComboBox);
            cargoPanel.add(toStationLabel);
            cargoPanel.add(toStationComboBox);
            cargoPanel.add(new JLabel()); // Пустая ячейка
            cargoPanel.add(sendCargoButton);

            // Панель для отслеживания состояния груза
            cargoStatusModel = new DefaultListModel<>();
            JList<String> cargoStatusList = new JList<>(cargoStatusModel);
            JScrollPane cargoStatusScrollPane = new JScrollPane(cargoStatusList);

            add(cargoPanel, BorderLayout.NORTH);
            add(cargoStatusScrollPane, BorderLayout.CENTER);

            // Обработчик отправки груза
            sendCargoButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String fromStation = (String) fromStationComboBox.getSelectedItem();
                    String toStation = (String) toStationComboBox.getSelectedItem();
                    if (fromStation != null && toStation != null && !fromStation.equals(toStation)) {
                        cargoManager.sendCargo(fromStation, toStation);
                        cargoStatusModel.addElement("Груз отправлен: " + fromStation + " -> " + toStation);
                    } else {
                        JOptionPane.showMessageDialog(null, "Выберите разные станции отправления и назначения!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }
    }

    // Класс для управления грузом
    class CargoManager {
        private ArrayList<String> cargoLog = new ArrayList<>();
        private ArrayList<String> stationCargoQueue = new ArrayList<>();

        // Метод для отправки груза
        public void sendCargo(String fromStation, String toStation) {
            String cargoStatus = "Груз отправлен от " + fromStation + " до " + toStation;
            stationCargoQueue.add(cargoStatus);
            cargoLog.add(cargoStatus);
            updateCargoFile(cargoStatus);
        }

        // Получить груз для станции
        public String getCargoForStation(String station) {
            for (String cargo : stationCargoQueue) {
                if (cargo.contains("от " + station)) {
                    stationCargoQueue.remove(cargo);
                    return cargo;
                }
            }
            return null;
        }

        // Метод для отправки груза дальше (оператор отправляет груз)
        public void dispatchCargo(String cargo) {
            cargoLog.add(cargo + " - отправлено оператором");
        }

        // Метод для записи состояния груза в файл
        private void updateCargoFile(String cargoStatus) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("cargo_status.txt", true))) {
                writer.write(cargoStatus);
                writer.newLine();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка записи состояния груза: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Класс "Администратор"
    class Administrator {
        private ArrayList<String> stations = new ArrayList<>();
        private HashMap<String, ArrayList<String>> operators = new HashMap<>();
        private ArrayList<String> trains = new ArrayList<>();

        // Метод для добавления станции и записи в файл
        public void addStation(String name) {
            if (!stations.contains(name)) {
                stations.add(name);
                operators.put(name, new ArrayList<>());
                writeToFile("stations.txt", name);
            } else {
                JOptionPane.showMessageDialog(null, "Станция " + name + " уже существует!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Метод для добавления оператора на станцию и записи в файл
        public void addOperatorToStation(String stationName, String operatorName) {
            ArrayList<String> stationOperators = operators.get(stationName);
            if (stationOperators != null) {
                if (!stationOperators.contains(operatorName)) {
                    stationOperators.add(operatorName);
                    writeToFile("operators.txt", "Оператор: " + operatorName + " на станции: " + stationName);
                } else {
                    JOptionPane.showMessageDialog(null, "Оператор " + operatorName + " уже добавлен на станцию " + stationName + "!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Станция " + stationName + " не найдена.");
            }
        }

        // Метод для добавления поезда и записи в файл
        public void addTrain(String trainNumber, String departure, String destination) {
            String trainInfo = "Поезд " + trainNumber + ": от " + departure + " до " + destination;
            if (!trains.contains(trainInfo)) {
                trains.add(trainInfo);
                writeToFile("trains.txt", "Поезд: " + trainNumber + " от " + departure + " до " + destination);
            } else {
                JOptionPane.showMessageDialog(null, "Поезд " + trainNumber + " уже существует!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Метод для проверки существования станции
        public boolean stationExists(String stationName) {
            return stations.contains(stationName);
        }

        // Метод для записи информации в файл
        private void writeToFile(String fileName, String content) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                writer.write(content);
                writer.newLine();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка записи в файл: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Метод для удаления информации из файла
        public void removeFromFile(String fileName, String content) {
            try {
                File inputFile = new File(fileName);
                File tempFile = new File("tempFile.txt");

                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    // Пропускаем строку, которую нужно удалить
                    if (!currentLine.trim().equals(content.trim())) {
                        writer.write(currentLine);
                        writer.newLine();
                    }
                }

                writer.close();
                reader.close();

                // Удаляем оригинальный файл и переименовываем временный файл
                if (!inputFile.delete()) {
                    JOptionPane.showMessageDialog(null, "Не удалось удалить оригинальный файл.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!tempFile.renameTo(inputFile)) {
                    JOptionPane.showMessageDialog(null, "Не удалось переименовать временный файл.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }

            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка удаления из файла: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Метод для загрузки данных из файла в модель списка
        public void loadFromFile(String fileName, DefaultListModel<String> model) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    model.addElement(line);
                    if (fileName.equals("stations.txt")) {
                        stations.add(line); // Добавление станции в список
                        operators.put(line, new ArrayList<>()); // Инициализация операторов для станции
                    }
                }
            } catch (FileNotFoundException e) {
                // Если файла нет, не выводим ошибку
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка чтения из файла: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Метод для загрузки станций в JComboBox
        public void loadStationsIntoComboBox(JComboBox<String> comboBox) {
            comboBox.removeAllItems();
            for (String station : stations) {
                comboBox.addItem(station);
            }
        }

        // Метод для загрузки станций в память (вызывается до загрузки в JComboBox)
        public void loadStations() {
            try (BufferedReader reader = new BufferedReader(new FileReader("stations.txt"))) {
                String line;
                stations.clear(); // Очистка списка станций перед загрузкой
                while ((line = reader.readLine()) != null) {
                    stations.add(line); // Добавляем станцию в список
                    operators.put(line, new ArrayList<>()); // Инициализируем пустой список операторов для станции
                }
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Файл stations.txt не найден.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка чтения файла stations.txt: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Класс для управления операторами
    class OperatorManager {
        private static final String OPERATORS_FILE = "operators_accounts.txt";
        private HashMap<String, String> operatorToStation = new HashMap<>();

        // Загрузка операторов из файла в память (с привязкой к станции)
        public void loadOperatorsFromFile() {
            try (BufferedReader reader = new BufferedReader(new FileReader(OPERATORS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] operatorData = line.split(",");
                    operatorToStation.put(operatorData[0], operatorData[2]); // Привязка оператора к станции
                }
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Файл операторов не найден.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка чтения файла операторов: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Регистрация оператора
        public void registerOperator(String operatorName, String stationName) {
            operatorToStation.put(operatorName, stationName);
            String password = operatorName + "123"; // Пароль: имя оператора + 123
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(OPERATORS_FILE, true))) {
                writer.write(operatorName + "," + password + "," + stationName);
                writer.newLine();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка создания учётной записи оператора: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Проверка логина и пароля оператора
        public boolean isValidOperator(String operatorName, String password) {
            try (BufferedReader reader = new BufferedReader(new FileReader(OPERATORS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] operatorData = line.split(",");
                    if (operatorData[0].equals(operatorName) && operatorData[1].equals(password)) {
                        return true;
                    }
                }
            } catch (FileNotFoundException e) {
                // Если файл не существует
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка чтения файла операторов: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }

        // Получить станцию для оператора
        public String getStationForOperator(String operatorName) {
            loadOperatorsFromFile(); // Загружаем операторов из файла, если они не загружены
            return operatorToStation.get(operatorName); // Получаем станцию оператора
        }
    }

    // Класс для управления пользователями
    class UserManager {
        private static final String USERS_FILE = "users.txt";

        // Метод для проверки существования пользователя
        public boolean userExists(String username) {
            try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] userData = line.split(",");
                    if (userData[0].equals(username)) {
                        return true;
                    }
                }
            } catch (FileNotFoundException e) {
                // Если файл не существует, считаем, что пользователи ещё не зарегистрированы
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка чтения файла пользователей: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }

        // Метод для проверки логина и пароля пользователя
        public boolean isValidUser(String username, String password) {
            try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] userData = line.split(",");
                    if (userData[0].equals(username) && userData[1].equals(password)) {
                        return true;
                    }
                }
            } catch (FileNotFoundException e) {
                // Если файл не существует
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка чтения файла пользователей: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }

        // Метод для регистрации нового пользователя
        public void registerUser(String username, String password) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
                writer.write(username + "," + password);
                writer.newLine();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка записи пользователя: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        RailwaySystem loginWindow = new RailwaySystem();
        loginWindow.setVisible(true);
    }
}

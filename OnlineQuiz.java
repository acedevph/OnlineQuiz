import java.util.*;
import java.io.*;

public class OnlineQuiz {

    static final String QUESTIONS_FILE = "questions.txt";
    static final int SECONDS_PER_QUESTION = 15;

    // ─────────────────────────────────────────────
    // Question class
    // ─────────────────────────────────────────────
    static class Question {
        private String text;
        private String[] choices;   // length 4
        private int correctIndex;   // 0-based

        public Question(String text, String[] choices, int correctIndex) {
            this.text = text;
            this.choices = choices;
            this.correctIndex = correctIndex;
        }

        public String getText() { return text; }
        public String[] getChoices() { return choices; }
        public int getCorrectIndex() { return correctIndex; }
        public char getCorrectLetter() { return (char)('A' + correctIndex); }

        /** File format: text | A | B | C | D | correctIndex (0-based) */
        public String toFileLine() {
            return text + "|" + choices[0] + "|" + choices[1] + "|" + choices[2] + "|"
                    + choices[3] + "|" + correctIndex;
        }

        public static Question fromFileLine(String line) {
            String[] p = line.split("\\|");
            if (p.length != 6) return null;
            String[] ch = { p[1].trim(), p[2].trim(), p[3].trim(), p[4].trim() };
            return new Question(p[0].trim(), ch, Integer.parseInt(p[5].trim()));
        }
    }

    // ─────────────────────────────────────────────
    // QuizEngine
    // ─────────────────────────────────────────────
    static class QuizEngine {
        private List<Question> questions = new ArrayList<>();

        public QuizEngine() {
            loadQuestionsFromFile();
            if (questions.isEmpty()) seedDefaultQuestions();
        }

        private void seedDefaultQuestions() {
            questions.add(new Question("What is the size of an int in Java?",
                    new String[]{"2 bytes","4 bytes","8 bytes","16 bytes"}, 1));
            questions.add(new Question("Which keyword is used for inheritance in Java?",
                    new String[]{"implements","inherits","extends","super"}, 2));
            questions.add(new Question("What does OOP stand for?",
                    new String[]{"Object Oriented Programming","Only One Process","Open Object Processing","Object Oriented Protocol"}, 0));
            questions.add(new Question("Which collection allows duplicate values?",
                    new String[]{"Set","HashMap","TreeSet","ArrayList"}, 3));
            questions.add(new Question("Which method is the entry point of a Java program?",
                    new String[]{"start()","run()","main()","init()"}, 2));
            questions.add(new Question("Which of the following is NOT a primitive type in Java?",
                    new String[]{"int","String","boolean","char"}, 1));
            questions.add(new Question("What is the output of: System.out.println(10 % 3);",
                    new String[]{"3","0","1","2"}, 2));
            questions.add(new Question("Which exception is thrown for dividing by zero in integer arithmetic?",
                    new String[]{"NullPointerException","ArithmeticException","NumberFormatException","IndexOutOfBoundsException"}, 1));
            questions.add(new Question("Which access modifier makes a member accessible only within its class?",
                    new String[]{"public","protected","default","private"}, 3));
            questions.add(new Question("What is the parent class of all Java classes?",
                    new String[]{"Class","Super","Object","Base"}, 2));
            saveQuestionsToFile();
            System.out.println("  [*] Default questions loaded and saved.");
        }

        public void runQuiz(String playerName) {
            if (questions.isEmpty()) { System.out.println("  [!] No questions available."); return; }
            List<Question> pool = new ArrayList<>(questions);
            Collections.shuffle(pool);

            int score = 0;
            int total = pool.size();
            List<String> results = new ArrayList<>();

            System.out.println("\n  ══════════════════════════════════════════════");
            System.out.println("  Quiz started! You have " + SECONDS_PER_QUESTION + " seconds per question.");
            System.out.println("  ══════════════════════════════════════════════\n");

            Scanner sc = new Scanner(System.in);
            for (int i = 0; i < total; i++) {
                Question q = pool.get(i);
                System.out.printf("  Q%d/%d: %s%n", i + 1, total, q.getText());
                String[] ch = q.getChoices();
                for (int c = 0; c < 4; c++)
                    System.out.printf("    [%c] %s%n", 'A' + c, ch[c]);

                long start = System.currentTimeMillis();
                System.out.print("  Your answer (A/B/C/D): ");
                String ans = sc.nextLine().trim().toUpperCase();
                long elapsed = (System.currentTimeMillis() - start) / 1000;

                if (elapsed > SECONDS_PER_QUESTION) {
                    System.out.println("  ⏰ Time's up! Moving on.");
                    results.add("Q" + (i+1) + ": TIME OUT | Correct: " + q.getCorrectLetter());
                    continue;
                }

                boolean correct = ans.length() == 1 && (ans.charAt(0) - 'A') == q.getCorrectIndex();
                if (correct) {
                    score++;
                    System.out.println("  ✔ Correct!\n");
                    results.add("Q" + (i+1) + ": CORRECT");
                } else {
                    System.out.println("  ✘ Wrong! Correct answer: " + q.getCorrectLetter()
                            + " - " + ch[q.getCorrectIndex()] + "\n");
                    results.add("Q" + (i+1) + ": WRONG | Correct: " + q.getCorrectLetter()
                            + " - " + ch[q.getCorrectIndex()]);
                }
            }

            // Final summary
            double pct = (double) score / total * 100;
            System.out.println("  ══════════════════════════════════════════════");
            System.out.printf("  Player : %s%n", playerName);
            System.out.printf("  Score  : %d / %d (%.1f%%)%n", score, total, pct);
            System.out.printf("  Remarks: %s%n", getRemarks(pct));
            System.out.println("  ── Item Analysis ──");
            results.forEach(r -> System.out.println("    " + r));
            System.out.println("  ══════════════════════════════════════════════");
        }

        private String getRemarks(double pct) {
            if (pct == 100)   return "PERFECT SCORE! Outstanding!";
            if (pct >= 90)    return "Excellent!";
            if (pct >= 75)    return "Very Good!";
            if (pct >= 60)    return "Good — keep studying!";
            if (pct >= 50)    return "Passing — needs improvement.";
            return "Failed — please review the material.";
        }

        public void addQuestion(Question q) {
            questions.add(q);
            saveQuestionsToFile();
            System.out.println("  [+] Question added. Total: " + questions.size());
        }

        private void saveQuestionsToFile() {
            try (PrintWriter pw = new PrintWriter(new FileWriter(QUESTIONS_FILE))) {
                for (Question q : questions) pw.println(q.toFileLine());
            } catch (IOException e) {
                System.out.println("  [!] Save error: " + e.getMessage());
            }
        }

        private void loadQuestionsFromFile() {
            File f = new File(QUESTIONS_FILE);
            if (!f.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Question q = Question.fromFileLine(line.trim());
                    if (q != null) questions.add(q);
                }
                System.out.println("  [*] Loaded " + questions.size() + " questions from file.");
            } catch (IOException e) {
                System.out.println("  [!] Load error: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        QuizEngine engine = new QuizEngine();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║          ONLINE QUIZ APPLICATION         ║");
        System.out.println("╚══════════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            System.out.println("\n  [1]  Start Quiz");
            System.out.println("  [2]  Add Question");
            System.out.println("  [3]  Exit");
            System.out.print("  Choice: ");
            String ch = sc.nextLine().trim();

            switch (ch) {
                case "1":
                    System.out.print("  Enter your name: ");
                    String name = sc.nextLine().trim();
                    if (name.isEmpty()) name = "Player";
                    engine.runQuiz(name);
                    break;
                case "2":
                    try {
                        System.out.print("  Question text: ");
                        String text = sc.nextLine().trim();
                        String[] choices = new String[4];
                        for (int i = 0; i < 4; i++) {
                            System.out.printf("  Choice %c: ", 'A' + i);
                            choices[i] = sc.nextLine().trim();
                        }
                        System.out.print("  Correct answer (0=A, 1=B, 2=C, 3=D): ");
                        int idx = Integer.parseInt(sc.nextLine().trim());
                        if (idx < 0 || idx > 3) { System.out.println("  [!] Index must be 0-3."); break; }
                        engine.addQuestion(new Question(text, choices, idx));
                    } catch (NumberFormatException e) {
                        System.out.println("  [!] Invalid index.");
                    }
                    break;
                case "3":
                    running = false;
                    System.out.println("  Thanks for playing!");
                    break;
                default:
                    System.out.println("  [!] Invalid choice.");
            }
        }
        sc.close();
    }
}
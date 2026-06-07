INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, difficulty, topic_tag)
SELECT 'Which keyword is used to declare a class in Java?', 'class', 'object', 'method', 'package', 'A', 'EASY', 'basic_classes'
WHERE NOT EXISTS (
	SELECT 1 FROM questions WHERE question_text = 'Which keyword is used to declare a class in Java?'
);

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, difficulty, topic_tag)
SELECT 'Which loop continues while its condition evaluates to true?', 'switch', 'while', 'return', 'import', 'B', 'EASY', 'loops'
WHERE NOT EXISTS (
	SELECT 1 FROM questions WHERE question_text = 'Which loop continues while its condition evaluates to true?'
);

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, difficulty, topic_tag)
SELECT 'Which symbol commonly ends a Java statement?', ';', ':', '#', '@', 'A', 'EASY', 'syntax'
WHERE NOT EXISTS (
	SELECT 1 FROM questions WHERE question_text = 'Which symbol commonly ends a Java statement?'
);

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, difficulty, topic_tag)
SELECT 'Which OOP feature allows multiple methods to share a name with different parameter lists?', 'Method overloading', 'Method hiding', 'Encapsulation', 'Serialization', 'A', 'MEDIUM', 'method_overloading'
WHERE NOT EXISTS (
	SELECT 1 FROM questions WHERE question_text = 'Which OOP feature allows multiple methods to share a name with different parameter lists?'
);

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, difficulty, topic_tag)
SELECT 'What is the primary purpose of a constructor in Java?', 'To destroy unused objects', 'To initialize a newly created object', 'To override inherited methods', 'To import external packages', 'B', 'MEDIUM', 'constructors'
WHERE NOT EXISTS (
	SELECT 1 FROM questions WHERE question_text = 'What is the primary purpose of a constructor in Java?'
);

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, difficulty, topic_tag)
SELECT 'Which keyword allows one Java class to inherit from another class?', 'implements', 'instanceof', 'extends', 'final', 'C', 'MEDIUM', 'inheritance'
WHERE NOT EXISTS (
	SELECT 1 FROM questions WHERE question_text = 'Which keyword allows one Java class to inherit from another class?'
);

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, difficulty, topic_tag)
SELECT 'During runtime polymorphism, which method implementation is selected?', 'The reference type method only', 'The actual object type overridden method', 'The first method in source order', 'The method with the shortest name', 'B', 'HARD', 'polymorphism'
WHERE NOT EXISTS (
	SELECT 1 FROM questions WHERE question_text = 'During runtime polymorphism, which method implementation is selected?'
);

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, difficulty, topic_tag)
SELECT 'What must a concrete class do when it implements an interface with abstract methods?', 'Declare every method as private', 'Convert the interface into a class', 'Provide implementations for the abstract methods', 'Remove all constructors', 'C', 'HARD', 'interfaces'
WHERE NOT EXISTS (
	SELECT 1 FROM questions WHERE question_text = 'What must a concrete class do when it implements an interface with abstract methods?'
);

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, difficulty, topic_tag)
SELECT 'When overriding a superclass method, which access-level rule must be followed?', 'The overriding method cannot be more restrictive', 'The overriding method must always be private', 'The overriding method must change its name', 'The overriding method cannot return any value', 'A', 'HARD', 'method_overriding'
WHERE NOT EXISTS (
	SELECT 1 FROM questions WHERE question_text = 'When overriding a superclass method, which access-level rule must be followed?'
);

INSERT INTO coding_questions (title, description, difficulty, test_cases_json)
SELECT
	'Implement a Factorial Method',
	'Write a Solution class with a public static String solve(String input) method. The input contains one non-negative integer n. Return n factorial as a decimal string. The hidden suite validates zero, small values, and larger loop behavior.',
	'EASY',
	'[{"input":"0","expected":"1"},{"input":"1","expected":"1"},{"input":"5","expected":"120"},{"input":"7","expected":"5040"}]'
WHERE NOT EXISTS (
	SELECT 1 FROM coding_questions WHERE title = 'Implement a Factorial Method'
);

INSERT INTO app_users (username, password, role)
SELECT 'student', '$2a$10$DWeCLA55ckAgbP/JA9a5FekxVZtJb/ejuz2zUWlehWgBO4CAtWzBu', 'STUDENT'
WHERE NOT EXISTS (
	SELECT 1 FROM app_users WHERE username = 'student'
);

INSERT INTO app_users (username, password, role)
SELECT 'teacher', '$2a$10$ip/CtIWPyfMcnTgSMoXOIeYcHRmiqNrlIaUhPiOzcVXnNpQ5l7eK.', 'TEACHER'
WHERE NOT EXISTS (
	SELECT 1 FROM app_users WHERE username = 'teacher'
);

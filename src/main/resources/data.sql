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

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, difficulty, topic_tag)
SELECT seed.question_text, seed.option_a, seed.option_b, seed.option_c, seed.option_d, seed.correct_option, seed.difficulty, seed.topic_tag
FROM (
	SELECT 'What is an object in Java?' AS question_text, 'A package that stores files' AS option_a, 'An instance of a class' AS option_b, 'A keyword used for loops' AS option_c, 'A comment in source code' AS option_d, 'B' AS correct_option, 'EASY' AS difficulty, 'objects' AS topic_tag
	UNION ALL
	SELECT 'Which keyword is commonly used to create a new object?', 'class', 'void', 'new', 'static', 'C', 'EASY', 'objects'
	UNION ALL
	SELECT 'Which part of a class usually stores object state?', 'Fields', 'Import statements', 'Package names', 'Comments', 'A', 'EASY', 'classes'
	UNION ALL
	SELECT 'Which access modifier makes a field accessible only inside its own class?', 'public', 'private', 'protected', 'static', 'B', 'EASY', 'encapsulation'
	UNION ALL
	SELECT 'What should a constructor name match in Java?', 'The package name', 'The file extension', 'The class name', 'The return type', 'C', 'EASY', 'constructors'
	UNION ALL
	SELECT 'Which method type is often used to read a private field value?', 'getter', 'main', 'constructor', 'importer', 'A', 'EASY', 'encapsulation'
	UNION ALL
	SELECT 'What does the keyword this refer to inside an instance method?', 'The parent class', 'The current object', 'The compiler', 'The package folder', 'B', 'EASY', 'objects'
	UNION ALL
	SELECT 'Which statement best describes encapsulation?', 'Keeping all classes in one file', 'Using only static methods', 'Hiding data and controlling access through methods', 'Deleting constructors from a class', 'C', 'EASY', 'encapsulation'
) seed
WHERE NOT EXISTS (
	SELECT 1 FROM questions existing_question WHERE existing_question.question_text = seed.question_text
);

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, difficulty, topic_tag)
SELECT seed.question_text, seed.option_a, seed.option_b, seed.option_c, seed.option_d, seed.correct_option, seed.difficulty, seed.topic_tag
FROM (
	SELECT 'What does super() call from a subclass constructor?' AS question_text, 'The subclass field list' AS option_a, 'The parent class constructor' AS option_b, 'The garbage collector' AS option_c, 'The main method' AS option_d, 'B' AS correct_option, 'MEDIUM' AS difficulty, 'inheritance' AS topic_tag
	UNION ALL
	SELECT 'When does method overriding occur?', 'A subclass provides a method with the same signature as a superclass method', 'Two fields share the same value', 'A class has no constructor', 'A method has no parameters', 'A', 'MEDIUM', 'method_overriding'
	UNION ALL
	SELECT 'Which access modifier allows access from subclasses and the same package?', 'private', 'protected', 'final', 'abstract', 'B', 'MEDIUM', 'encapsulation'
	UNION ALL
	SELECT 'What is composition in object-oriented design?', 'A class containing references to other objects as parts', 'A loop inside a method', 'A class without fields', 'A method returning void', 'A', 'MEDIUM', 'composition'
	UNION ALL
	SELECT 'What does an interface primarily define in Java?', 'A contract of methods that classes can implement', 'A folder for compiled class files', 'A database table', 'A replacement for every constructor', 'A', 'MEDIUM', 'interfaces'
	UNION ALL
	SELECT 'What is upcasting?', 'Treating a subclass object as a superclass reference', 'Converting an int to a String', 'Making a private field public', 'Calling a static method from main', 'A', 'MEDIUM', 'polymorphism'
	UNION ALL
	SELECT 'What does dynamic method dispatch support?', 'Choosing overridden methods at runtime based on the object type', 'Sorting imports alphabetically', 'Creating packages automatically', 'Skipping constructor calls', 'A', 'MEDIUM', 'polymorphism'
	UNION ALL
	SELECT 'Why are getters and setters commonly used?', 'To control access to private fields', 'To remove all methods from a class', 'To make every class abstract', 'To prevent object creation always', 'A', 'MEDIUM', 'encapsulation'
) seed
WHERE NOT EXISTS (
	SELECT 1 FROM questions existing_question WHERE existing_question.question_text = seed.question_text
);

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, difficulty, topic_tag)
SELECT seed.question_text, seed.option_a, seed.option_b, seed.option_c, seed.option_d, seed.correct_option, seed.difficulty, seed.topic_tag
FROM (
	SELECT 'What does marking a class final prevent?' AS question_text, 'Creating objects from it' AS option_a, 'Declaring fields inside it' AS option_b, 'Extending it with a subclass' AS option_c, 'Calling its public methods' AS option_d, 'C' AS correct_option, 'HARD' AS difficulty, 'inheritance' AS topic_tag
	UNION ALL
	SELECT 'Can a private method be overridden by a subclass in Java?', 'Yes, if the names match', 'No, private methods are not inherited for overriding', 'Yes, only when static', 'No, because classes cannot have private methods', 'B', 'HARD', 'method_overriding'
	UNION ALL
	SELECT 'What is a covariant return type?', 'An overridden method returning a subtype of the original return type', 'A constructor returning void', 'A field that changes from private to public', 'A loop that returns early', 'A', 'HARD', 'polymorphism'
	UNION ALL
	SELECT 'What does the Liskov Substitution Principle require?', 'Subclasses should be usable wherever their superclass is expected', 'Every class must be final', 'All fields must be public', 'Constructors must be overloaded', 'A', 'HARD', 'inheritance'
	UNION ALL
	SELECT 'How are instance fields resolved when a subclass hides a superclass field?', 'By the declared reference type', 'By the newest constructor only', 'By the package name', 'By the number of parameters', 'A', 'HARD', 'field_hiding'
	UNION ALL
	SELECT 'Which statement about constructor chaining is true?', 'A call to this() or super() must be the first statement when used', 'Constructors can be called after return', 'Constructors are inherited like methods', 'Every constructor must be private', 'A', 'HARD', 'constructors'
	UNION ALL
	SELECT 'Why can an abstract method not be private?', 'It must be visible to subclasses that implement it', 'Private methods are always constructors', 'Abstract methods must be static', 'Java does not allow private fields', 'A', 'HARD', 'abstraction'
	UNION ALL
	SELECT 'What is one benefit of enforcing invariants through encapsulation?', 'Object state can be validated before it changes', 'All methods become faster automatically', 'Inheritance is disabled', 'Packages are renamed automatically', 'A', 'HARD', 'encapsulation'
) seed
WHERE NOT EXISTS (
	SELECT 1 FROM questions existing_question WHERE existing_question.question_text = seed.question_text
);

INSERT INTO coding_questions (title, description, difficulty, test_cases_json)
SELECT seed.title, seed.description, seed.difficulty, seed.test_cases_json
FROM (
	SELECT
		'Sum of Two Numbers' AS title,
		'Write a method that takes two comma-separated integers as a string and returns their sum as a string.' AS description,
		'EASY' AS difficulty,
		'[{"input":"5,7","expected":"12"},{"input":"0,0","expected":"0"},{"input":"-3,9","expected":"6"},{"input":"10,-4","expected":"6"},{"input":"100,250","expected":"350"}]' AS test_cases_json
	UNION ALL
	SELECT
		'Even or Odd',
		'Write a method that takes an integer string and returns ''Even'' or ''Odd''.',
		'EASY',
		'[{"input":"4","expected":"Even"},{"input":"7","expected":"Odd"},{"input":"0","expected":"Even"},{"input":"-2","expected":"Even"},{"input":"15","expected":"Odd"}]'
) seed
WHERE NOT EXISTS (
	SELECT 1 FROM coding_questions existing_question WHERE existing_question.title = seed.title
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

INSERT INTO coding_questions (title, description, difficulty, test_cases_json)
SELECT
	'Implement a String Reversal Method',
	'Write a Solution class with a public static String solve(String input) method. The input contains one string value. Return the characters in reverse order exactly as a string. The hidden suite validates short words, mixed characters, and spacing behavior.',
	'EASY',
	'[{"input":"hello","expected":"olleh"},{"input":"OOP","expected":"POO"},{"input":"Java 25","expected":"52 avaJ"},{"input":"racecar","expected":"racecar"}]'
WHERE NOT EXISTS (
	SELECT 1 FROM coding_questions WHERE title = 'Implement a String Reversal Method'
);

SET @student_name_column_exists := (
	SELECT COUNT(*)
	FROM information_schema.columns
	WHERE table_schema = DATABASE()
		AND table_name = 'quiz_results'
		AND column_name = 'student_name'
);
SET @student_name_nullable_sql := IF(
	@student_name_column_exists > 0,
	'ALTER TABLE quiz_results MODIFY COLUMN student_name varchar(120) NULL',
	'SELECT 1'
);
PREPARE student_name_nullable_statement FROM @student_name_nullable_sql;
EXECUTE student_name_nullable_statement;
DEALLOCATE PREPARE student_name_nullable_statement;

UPDATE app_users
SET
	first_name = COALESCE(first_name, 'Default'),
	last_name = COALESCE(last_name, 'Teacher'),
	email = COALESCE(email, 'teacher@oopstudio.local'),
	unique_teacher_id = COALESCE(unique_teacher_id, 'T-1000'),
	username = 'T-1000'
WHERE username = 'teacher' OR unique_teacher_id = 'T-1000';

INSERT INTO app_users (first_name, last_name, email, unique_teacher_id, username, password, role)
SELECT
	'Default',
	'Teacher',
	'teacher@oopstudio.local',
	'T-1000',
	'T-1000',
	'$2a$10$ip/CtIWPyfMcnTgSMoXOIeYcHRmiqNrlIaUhPiOzcVXnNpQ5l7eK.',
	'TEACHER'
WHERE NOT EXISTS (
	SELECT 1 FROM app_users WHERE email = 'teacher@oopstudio.local' OR unique_teacher_id = 'T-1000'
);

UPDATE app_users
SET
	first_name = COALESCE(first_name, 'Default'),
	last_name = COALESCE(last_name, 'Student'),
	email = COALESCE(email, 'student@oopstudio.local'),
	unique_teacher_id = NULL,
	username = 'student@oopstudio.local',
	supervisor_id = (
		SELECT teacher_ref.id
		FROM (SELECT id FROM app_users WHERE unique_teacher_id = 'T-1000' LIMIT 1) teacher_ref
	)
WHERE username = 'student' OR email = 'student@oopstudio.local';

INSERT INTO app_users (first_name, last_name, email, username, password, role, supervisor_id)
SELECT
	'Default',
	'Student',
	'student@oopstudio.local',
	'student@oopstudio.local',
	'$2a$10$DWeCLA55ckAgbP/JA9a5FekxVZtJb/ejuz2zUWlehWgBO4CAtWzBu',
	'STUDENT',
	(SELECT id FROM app_users WHERE unique_teacher_id = 'T-1000' LIMIT 1)
WHERE NOT EXISTS (
	SELECT 1 FROM app_users WHERE email = 'student@oopstudio.local'
);

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.File;
import java.util.Locale;
	
public class main {
	private static final Scanner INPUT = new Scanner(System.in);
	private static final File RECIPES_FILE = new File("recipes.json");
	private static final List<String> MEASUREMENT_UNITS = List.of(
		"tsp", "tbsp", "pinch", "cup", "ounce", "gram", "liter"
	);

	public static void main(String[] args) {
		boolean running = true;

		System.out.println("Welcome to");
		System.out.println(" ____           _             __        ___                  _ ");
		System.out.println("|  _ \\ ___  ___(_)_ __   ___  \\ \\      / (_)______ _ _ __ __| |");
		System.out.println("| |_) / _ \\/ __| | '_ \\ / _ \\  \\ \\ /\\ / /| |_  / _` | '__/ _` |");
		System.out.println("|  _ <  __/ (__| | |_) |  __/   \\ V  V / | |/ / (_| | | | (_| |");
		System.out.println("|_| \\_\\___|\\___|_| .__/ \\___|    \\_/\\_/  |_/___\\__,_|_|  \\__,_|");
		System.out.println("                 |_|                                           ");
		System.out.println();
		System.out.println("+----------------------+-------------------+");
		

		while (running) {
			System.out.println("\nRecipe Converter");
			System.out.println("1. Choose an existing recipe");
			System.out.println("2. Add a recipe");
			System.out.println("3. Quick converter lookup");
			System.out.println("4. Exit");
			System.out.println("5. Create a new type from an existing recipe");
			System.out.print("Choose an option: ");

			String choice = INPUT.nextLine().trim();
			switch (choice) {
				case "1" -> showRecipes();
				case "2" -> addRecipe();
				case "3" -> quickLookup();
				case "4" -> running = false;
				case "5" -> createRecipeVariation();
				default -> System.out.println("Enter your choice.");
			}
		}

		System.out.println("Goodbye!");
	}

	// Show the list of recipes based on the selected diet type
	// Allows the user to choose a recipe from the filtered list
	// Displays the ingredients and instructions for the selected recipe
	private static void showRecipes() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(RECIPES_FILE);
			JsonNode recipeList = root == null ? null : root.get("recipes");
			if (!(recipeList instanceof ArrayNode)) {
				System.out.println("Error reading recipes: recipes.json must have a recipes array.");
				return;
			}

			System.out.println("\nChoose a recipe type:");
			System.out.println("1. Show all");
			System.out.println("2. Vegan");
			System.out.println("3. Gluten free");
			System.out.println("4. Dairy free");
			System.out.println("5. Originals");
			System.out.print("Choose an option: ");
			String choice = INPUT.nextLine().trim();
			String selectedDiet = switch (choice) {
				case "2" -> "vegan";
				case "3" -> "gluten free";
				case "4" -> "dairy free";
				case "5" -> "regular";
				case "1" -> "all";
				default -> null;
			};
			if (selectedDiet == null) {
				System.out.println("Invalid recipe type.");
				return;
			}
      // Filter recipes based on the selected diet type
			// Initialize a list to store matching recipes
			List<JsonNode> matchingRecipes = new ArrayList<>();
			for (JsonNode recipe : recipeList) {
				String diet = recipe.path("diet").asText("");
				if (selectedDiet.equals("all") || selectedDiet.equalsIgnoreCase(diet)) {
					matchingRecipes.add(recipe);
				}
			}
			if (matchingRecipes.isEmpty()) {
				System.out.println("No recipes found for that type.");
				return;
			}
			// Display the list of matching recipes for the user to choose from
			chooseRecipe(matchingRecipes);
		} catch (IOException e) {
			System.out.println("Error reading recipes: " + e.getMessage());
		}
	}
  // Allows the user to choose a recipe from the filtered list
	private static void chooseRecipe(List<JsonNode> recipes) {
		while (true) {
			System.out.println("\nSelect a recipe (enter 0 to cancel):");
			for (int index = 0; index < recipes.size(); index++) {
				System.out.println((index + 1) + ". "
					+ recipes.get(index).path("name").asText("Untitled recipe"));
			}
			// Prompt the user to select a recipe from the list
			System.out.print("Recipe number: ");
			String choice = INPUT.nextLine().trim();
			if (choice.equals("0")) {
				return;
			}
			int recipeIndex;
			try {
				recipeIndex = Integer.parseInt(choice) - 1;
			} catch (NumberFormatException e) {
				System.out.println("Invalid input. Please enter a number.");
				continue;
			}
			if (recipeIndex < 0 || recipeIndex >= recipes.size()) {
				System.out.println("Invalid recipe number.");
				continue;
			}
			displayRecipe(recipes.get(recipeIndex));
			System.out.print("\nPress Enter to return to the menu...");
			INPUT.nextLine();
			return;
		}
	}
  // Display the details of a single recipe, including ingredients and steps
	private static void displayRecipe(JsonNode recipe) {
		System.out.println("\n" + recipe.path("name").asText("Untitled recipe"));
		System.out.println("Type: " + recipe.path("diet").asText("unspecified"));
		System.out.println("\nIngredients:");
		printIngredients(recipe.path("ingredients"));

		System.out.println("\nSteps:");
		JsonNode instructions = recipe.path("instructions");
		if (instructions.isArray()) {
			for (int index = 0; index < instructions.size(); index++) {
				System.out.println((index + 1) + ". " + instructions.get(index).asText());
			}
		} else {
			String[] steps = instructions.asText("").split("\\R");
			int stepNumber = 1;
			for (String step : steps) {
				if (!step.isBlank()) {
					System.out.println(stepNumber++ + ". " + step.trim());
				}
			}
		}
	}
  // Print the list of ingredients for a recipe
	private static void printIngredients(JsonNode ingredients) {
		if (ingredients.isArray()) {
			for (int index = 0; index < ingredients.size(); index++) {
				printIngredient(index + 1, ingredients.get(index));
			}
		}
	}

	// Print the details of a single ingredient, including amount, unit, and name
	private static void printIngredient(int number, JsonNode ingredient) {
		if (ingredient.isObject()) {
			String amount = ingredient.path("amount").isNumber()
				? ingredient.path("amount").decimalValue().stripTrailingZeros().toPlainString()
				: ingredient.path("amount").asText("");
			System.out.println(number + ". " + amount + " "
				+ ingredient.path("unit").asText() + " "
				+ ingredient.path("name").asText("Unnamed ingredient"));
		} else {
			System.out.println(number + ". " + ingredient.asText());
		}
	}

	// Add a new recipe to the recipes.json file
	private static void addRecipe() {
		ObjectMapper mapper = new ObjectMapper();
		System.out.println("\nAdd a recipe");
		System.out.print("Recipe name: ");
		String name = INPUT.nextLine().trim();
		if (name.isEmpty()) {
			System.out.println("Recipe name cannot be empty.");
			return;
		}
		// Prompt the user to enter the ingredients for the recipe
		ArrayNode ingredients = enterIngredients(mapper, name);

		System.out.println("\nIngredients complete. Now add instructions.");
		System.out.print("Diet (regular, dairy free, vegan, gluten free): ");
		String diet = INPUT.nextLine().trim();
		ArrayNode instructions = enterInstructions(mapper);

		try {
			// Read the existing recipes from the file
			JsonNode root = mapper.readTree(RECIPES_FILE);
			if (!(root instanceof ObjectNode)) {
				System.out.println("Error saving recipe: recipes.json must contain a JSON object.");
				return;
			}
			JsonNode recipeList = root.get("recipes");
			if (!(recipeList instanceof ArrayNode)) {
				System.out.println("Error saving recipe: recipes.json must have a recipes array.");
				return;
			}
			// Create a new recipe object and add it to the recipes array
			ObjectNode recipe = mapper.createObjectNode();
			recipe.put("name", name);
			recipe.put("diet", diet);
			recipe.set("ingredients", ingredients);
			recipe.set("instructions", instructions);
			((ArrayNode) recipeList).add(recipe);
			// Add the new recipe to the recipes array and save it to the file
			mapper.writerWithDefaultPrettyPrinter().writeValue(RECIPES_FILE, root);
			System.out.println("Saved recipe: " + name);
		} catch (IOException e) {
			System.out.println("Error saving recipe: " + e.getMessage());
		}
	}
  // Prompt the user to enter ingredients for the recipe, including amount and unit
	private static ArrayNode enterIngredients(ObjectMapper mapper, String recipeName) {
		ArrayNode ingredients = mapper.createArrayNode();
		while (true) {
			System.out.printf("\nRecipe: %s | Ingredients added: %d%n", recipeName, ingredients.size());
			System.out.print("Ingredient name (blank when finished): ");
			String ingredientName = INPUT.nextLine().trim();
			if (ingredientName.isEmpty()) {
				if (ingredients.isEmpty()) {
					System.out.println("Add at least one ingredient before finishing.");
					continue;
				}
				break;
			}
      // Read the amount and unit for the ingredient from the user
			ObjectNode ingredient = readMeasuredIngredient(mapper, ingredientName);
			ingredients.add(ingredient);
			System.out.printf("Added: %s %s %s%n",
				ingredient.path("amount").asText(), ingredient.path("unit").asText(), ingredientName);
		}
		return ingredients;
	}
 
	private static ObjectNode readMeasuredIngredient(ObjectMapper mapper, String ingredientName) {
		double amount;
		while (true) {
			System.out.print("Amount (positive number): ");
			String amountInput = INPUT.nextLine().trim();
			try {
				amount = Double.parseDouble(amountInput);
				if (Double.isFinite(amount) && amount > 0) {
					break;
				}
			} catch (NumberFormatException ignored) {
			}
			System.out.println("Enter a positive numeric amount, such as 1 or 0.5.");
		}
		// Prompt the user to enter the measurement unit for the ingredient
		String unit;
		while (true) {
			System.out.print("Measurement (tsp, tbsp, pinch, cup, ounce, gram, liter): ");
			unit = INPUT.nextLine().trim().toLowerCase(Locale.ROOT);
			if (MEASUREMENT_UNITS.contains(unit)) {
				break;
			}
			System.out.println("Choose one of the listed measurement types.");
		}

		// Create an ObjectNode to represent the ingredient with the specified name, amount, and unit
		ObjectNode ingredient = mapper.createObjectNode();
		ingredient.put("name", ingredientName);
		ingredient.put("amount", amount);
		ingredient.put("unit", unit);
		// Return the ingredient object with name, amount, and unit fields
		return ingredient;
	}

  // Prompt the user to enter the instructions for the recipe, one step at a time
	private static ArrayNode enterInstructions(ObjectMapper mapper) {
		ArrayNode instructions = mapper.createArrayNode();
		System.out.println("Enter steps one at a time. Leave blank when finished.");
		int stepNumber = 1;
		while (true) {
			// Prompt the user to enter the next instruction step
			System.out.print("Step " + stepNumber + " (blank when finished): ");
			String step = INPUT.nextLine().trim();
			if (step.isEmpty()) {
				break;
			}
			// Add the entered step to the instructions array
			instructions.add(step);
			stepNumber++;
		}
		return instructions;
	}

  // Create a variation of an existing recipe with a new diet type
	private static void createRecipeVariation() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode root = mapper.readTree(RECIPES_FILE);
			JsonNode recipesNode = root == null ? null : root.get("recipes");
			if (!(recipesNode instanceof ArrayNode recipes)) {
				System.out.println("Error reading recipes: recipes.json must have a recipes array.");
				return;
			}
			if (recipes.isEmpty()) {
				System.out.println("There are no recipes to create a variation from.");
				return;
			}
      // Display the list of available recipes for the user to choose from
			System.out.println("\nChoose a recipe to copy:");
			for (int index = 0; index < recipes.size(); index++) {
				JsonNode recipe = recipes.get(index);
				System.out.printf("%d. %s (%s)%n", index + 1,
					recipe.path("name").asText("Untitled recipe"),
					recipe.path("diet").asText("unspecified"));
			}
			// Prompt the user to select a recipe to copy
			int sourceNumber = readMenuNumber("Recipe number (0 to cancel): ", 0, recipes.size());
			if (sourceNumber == 0) {
				return;
			}
      // Get the source recipe object to copy
			JsonNode source = recipes.get(sourceNumber - 1);
			if (!(source instanceof ObjectNode sourceObject)) {
				System.out.println("That recipe cannot be edited because it is not a JSON object.");
				return;
			}
      // Extract the recipe name and diet type from the source recipe
			String recipeName = source.path("name").asText("Untitled recipe");
			String sourceDiet = source.path("diet").asText("");
			JsonNode dietsNode = root.path("diets");
			List<String> newTypes = new ArrayList<>();
			if (dietsNode.isArray()) {
				for (JsonNode dietNode : dietsNode) {
					String diet = dietNode.asText().trim();
					if (!diet.isEmpty() && !diet.equalsIgnoreCase(sourceDiet)
							&& !hasRecipeType(recipes, recipeName, diet)) {
						newTypes.add(diet);
					}
				}
			}
			if (newTypes.isEmpty()) {
				System.out.println("No unused recipe types are available for this recipe.");
				return;
			}
      // Prompt the user to choose the new recipe type
			System.out.println("\nChoose the new recipe type:");
			for (int index = 0; index < newTypes.size(); index++) {
				System.out.println((index + 1) + ". " + newTypes.get(index));
			}
			int typeNumber = readMenuNumber("Type number (0 to cancel): ", 0, newTypes.size());
			if (typeNumber == 0) {
				return;
			}
			String newType = newTypes.get(typeNumber - 1);

      // Create a deep copy of the source recipe to modify as the new variation
			ObjectNode variation = sourceObject.deepCopy();
			variation.put("diet", newType);
			System.out.println("\nCreating " + newType + " version of " + recipeName + ".");
			editVariationIngredients(mapper, variation, recipeName);
			if (askYesNo("Replace the instructions? (y/n): ")) {
				variation.set("instructions", enterInstructions(mapper));
			}
      // Add the new variation to the list of recipes and save it to the file
			recipes.add(variation);
			mapper.writerWithDefaultPrettyPrinter().writeValue(RECIPES_FILE, root);
			// Notify the user that the variation has been saved
			System.out.printf("Saved %s version of %s with %d ingredients.%n",
				newType, recipeName, variation.path("ingredients").size());
		} catch (IOException e) {
			System.out.println("Error creating recipe variation: " + e.getMessage());
		}
	}
  // Edit the ingredients of a recipe variation based on user choice
	private static void editVariationIngredients(ObjectMapper mapper, ObjectNode variation, String recipeName) {
		System.out.println("\nCopied ingredients:");
		printIngredients(variation.path("ingredients"));
		System.out.println("\nIngredient changes:");
		System.out.println("1. Replace all ingredients");
		System.out.println("2. Add ingredients to the copied list");
		System.out.println("3. Review each ingredient");
		System.out.println("4. Keep copied ingredients");
		int choice = readMenuNumber("Choose an option: ", 1, 4);
		if (choice == 4) {
			return;
		}
		if (choice == 3) {
			reviewIngredients(mapper, variation, recipeName);
			return;
		}
    // Enter new ingredients based on the user's choice (replace all or add to copied list)
		ArrayNode enteredIngredients = enterIngredients(mapper, recipeName);
		if (choice == 1) {
			variation.set("ingredients", enteredIngredients);
		} else {
			// Combine the copied ingredients with the newly entered ingredients
			JsonNode copiedIngredients = variation.path("ingredients");
			ArrayNode combinedIngredients = copiedIngredients instanceof ArrayNode array
				? array : mapper.createArrayNode();
			combinedIngredients.addAll(enteredIngredients);
			variation.set("ingredients", combinedIngredients);
		}
		System.out.println("\nUpdated ingredients:");
		printIngredients(variation.path("ingredients"));
	}

	// Review and modify each ingredient of a recipe variation
	private static void reviewIngredients(ObjectMapper mapper, ObjectNode variation, String recipeName) {
		JsonNode original = variation.path("ingredients");
		if (!original.isArray() || original.isEmpty()) {
			System.out.println("There are no copied ingredients to review.");
			variation.set("ingredients", enterIngredients(mapper, recipeName));
			return;
		}
		
    // For each ingredient in the original list, prompt the user to keep, replace, or remove it
		ArrayNode reviewed = mapper.createArrayNode();
		for (int index = 0; index < original.size(); index++) {
			JsonNode ingredient = original.get(index);
			System.out.println("\nIngredient " + (index + 1) + " of " + original.size() + ":");
			printIngredient(index + 1, ingredient);
			if (askYesNo("Keep this ingredient? (y/n): ")) {
				reviewed.add(ingredient.deepCopy());
				continue;
			}

			System.out.print("Replacement ingredient name (blank to keep the original): ");
			String replacementName = INPUT.nextLine().trim();
			if (replacementName.isEmpty()) {
				reviewed.add(ingredient.deepCopy());
				continue;
			}
			// Read the replacement ingredient with the specified name from the user
			reviewed.add(readMeasuredIngredient(mapper, replacementName));
		}
    // Set the reviewed ingredients as the new ingredients for the variation
		variation.set("ingredients", reviewed);
		System.out.println("\nUpdated ingredients:");
		printIngredients(reviewed);
	}

  // Read a menu number from the user within the specified range
	private static int readMenuNumber(String prompt, int minimum, int maximum) {
		while (true) {
			System.out.print(prompt);
			String input = INPUT.nextLine().trim();
			try {
				int number = Integer.parseInt(input);
				if (number >= minimum && number <= maximum) {
					return number;
				}
			} catch (NumberFormatException ignored) {
			}
			System.out.println("Enter a number from " + minimum + " to " + maximum + ".");
		}
	}

	// Prompt the user with a yes/no question and return true for yes, false for no
	private static boolean askYesNo(String prompt) {
		while (true) {
			System.out.print(prompt);
			String answer = INPUT.nextLine().trim();
			if (answer.equalsIgnoreCase("y")) {
				return true;
			}
			if (answer.equalsIgnoreCase("n")) {
				return false;
			}
			System.out.println("Enter y or n.");
		}
	}

	// Check if a recipe with the specified name and diet type exists in the recipes array
	private static boolean hasRecipeType(ArrayNode recipes, String name, String diet) {
		for (JsonNode recipe : recipes) {
			if (recipe.path("name").asText().equalsIgnoreCase(name)
					&& recipe.path("diet").asText().equalsIgnoreCase(diet)) {
				return true;
			}
		}
		return false;
	}

	// Provide quick ingredient substitutions for common ingredients
	private static void quickLookup() {
		System.out.println("Quick lookups:");
		System.out.println("- 1 large egg: about 1/2 mashed banana in many baked goods");
		System.out.println("- 1 cup all-purpose flour: about 1 cup gluten-free flour blend");
		System.out.println("- 1 cup milk: about 1 cup almond milk or other plant-based milk");
		System.out.println("- 1/2 cup butter: about 1/2 cup coconut oil or vegan butter");
		System.out.println("- 1 cup sugar: about 3/4 cup coconut sugar or other natural sweeteners");
		System.out.println("- 1 cup butter: about 1 cup vegan butter or coconut oil");
	}
}

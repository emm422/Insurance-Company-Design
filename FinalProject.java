import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Calendar;
import java.util.Date;

public class FinalProject {
    static final String DB_URL = "jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241";
/************************************************************************************************************************************************************************/
    public static void main(String[] args){ 
        //initialize connection and prepared statements
        Connection conn = null;
        PreparedStatement pStmt = null;
        PreparedStatement pStmt1 = null;
        PreparedStatement pStmt2 = null;
        PreparedStatement pStmt3 = null;
        //initialize the scanner
        Scanner input = new Scanner(System.in);
        do{
            try{
                //prompt user for user id and password
                System.out.print("Enter Oracle user id: ");
                String user = input.nextLine();
                System.out.print("Enter Oracle user password: ");
                String pass = input.nextLine();
                //open the connection
                conn = DriverManager.getConnection(DB_URL, user, pass);
                System.out.println("Connected.");
                boolean correct = false;
                //prompt user to pick interface
                //keep prompting until the user inputs a correct option
                //call the method associated with the selected interface
                do{
                    System.out.println();
                    System.out.println("Main Menu: ");
                    System.out.println("Input the number of the desired interface: ");
                    System.out.println("1.) Customer");
                    System.out.println("2.) Agent");
                    System.out.println("3.) Adjuster");
                    System.out.println("4.) Coporate Management");
                    System.out.println("5.) Exit.");
                    String choice = input.nextLine();
                    if(choice.equals("1")){
                        customer(conn, pStmt, pStmt1, pStmt2, pStmt3, input);
                    }
                    else if(choice.equals("2")){
                        agent(conn, pStmt, input);
                    }
                    else if(choice.equals("3")){
                        adjuster(conn, pStmt, input);
                    }
                    else if(choice.equals("4")){
                        corporate(conn, pStmt, input);
                    }
                    else if(choice.equals("5")){
                        System.out.println("Thank you for using this application.");
                        //close connections and end program
                        conn.close();
                        input.close();
                        correct = true;
                    }
                    else{
                        System.out.println("Enter a correct input option. Please try again.");
                    }
                }while (correct == false);
            }
            //if the login info was incorrect, go back to top of outer do while loop and reprompt for login credentials again
            catch(SQLException se){
                System.out.println("[Error]: Connect error. Re-enter login data: ");
            }
        }while(conn == null);
    }

/************************************************************************************************************************************************************************/

    public static void customer(Connection conn, PreparedStatement pStmt, PreparedStatement pStmt1, PreparedStatement pStmt2, PreparedStatement pStmt3, Scanner input){
        System.out.println();
        System.out.println("Entering the Customer interface.");
        boolean correct1 = false;
        //display the menu options
        //prompt user to select an option
        //continue to do so until the user enters a proper option
        //stay in this interface until the user chooses
        do{
            System.out.println();
            System.out.println("Select an operation: ");
            System.out.println("1.) Create a new customer account.");
            System.out.println("2.) View policies.");
            System.out.println("3.) Buy a policy.");
            System.out.println("4.) Cancel a policy.");
            System.out.println("5.) Pay for a policy.");
            System.out.println("6.) Make a claim.");
            System.out.println("7.) Add an item to a policy.");
            System.out.println("8.) Add a person to a policy.");
            System.out.println("9.) Remove an item from a policy.");
            System.out.println("10.) Remove a person from a policy.");
            System.out.println("11.) Exit Customer interface.");
            String choice = input.nextLine();
            switch (choice){
                case "1":
                    try{
                        //create a new account for a user
                        //prompt user to enter first and last name
                        String first = getString(1, input);
                        String last = getString(2, input);
                        int id = (int)(Math.random() * (999999999 - 1 + 1) + 1);
                        pStmt = conn.prepareStatement("select customer_id from customer order by customer_id asc");
                        ResultSet rset = pStmt.executeQuery();
                        while(rset.next()){
                            if(rset.getInt("customer_id") == id){
                                id++;
                            }
                        }
                        pStmt = conn.prepareStatement("insert into customer (customer_id, first_name, last_name) values (?, ?, ?)");
                        pStmt.setInt(1,id);
                        pStmt.setString(2,first);
                        pStmt.setString(3,last);
                        pStmt.executeUpdate();
                        pStmt = conn.prepareStatement("select MAX(agent_id) from agent");
                        rset = pStmt.executeQuery();
                        int agent_id = 0;
                        while(rset.next()){
                            agent_id = rset.getInt("MAX(agent_id)");
                        }
                        //assign the new user an agent
                        pStmt = conn.prepareStatement("insert into advises (customer_id, agent_id) values (?, ?)");
                        pStmt.setInt(1, id);
                        pStmt.setInt(2, agent_id);
                        int x = pStmt.executeUpdate();
                        //display the new customer's Customer Id
                        if(x == 1){
                            System.out.println("Account creation successful.");
                            System.out.println("Customer Id: "+id);
                        }
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "2":
                    try{
                        //view a customer's policy based on the input Customer Id
                        //call the method to get the Customer Id
                        int customer_id = getId(1, input);
                        pStmt = conn.prepareStatement("select * from buys natural join customer natural join policy where customer_id = (?)");
                        pStmt.setInt(1, customer_id);
                        ResultSet rset = pStmt.executeQuery();
                        int i = 0;
                        //display information about the policies
                        while(rset.next()){
                            i++;
                            int policy_id = rset.getInt("policy_id");
                            String first_name = rset.getString("first_name");
                            String last_name = rset.getString("last_name");
                            Date start_date = rset.getDate("start_date");
                            Date end_date = rset.getDate("end_date");
                            int premium = rset.getInt("premium");
                            System.out.print(i+".) Name: "+first_name+" "+last_name+"\t\tPolicy Id: "+policy_id+"\t\tPremium: "+premium+"\t\tStart Date: "+start_date);
                            if(end_date != null){
                                System.out.println("\t\tEnd Date: "+end_date);
                            }
                            else System.out.println();
                        }    
                        if(i == 0) System.out.println("No policies match the entered Id.");
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "3":
                    try{
                        //buy a policy
                        //prompt the user to enter their Customer Id
                        int customer_id = getId(1, input);
                        pStmt = conn.prepareStatement("select * from customer where customer_id = (?)");
                        pStmt.setInt(1, customer_id);
                        ResultSet rset = pStmt.executeQuery();
                        if(rset.next()){
                            boolean correct4 = false; 
                            int policy_id = (int)(Math.random() * (999999999 - 1 + 1) + 1);
                            pStmt = conn.prepareStatement("select policy_id from policy order by policy_id asc");
                            rset = pStmt.executeQuery();
                            while(rset.next()){
                                if(rset.getInt("policy_id") == policy_id){
                                    policy_id++;
                                }
                            }
                            boolean correct = false;
                            int premium = 0;
                            //prompt the user to select a policy plan
                            do{
                                System.out.println("Select the plan you would like to buy.");
                                System.out.println("1.) Covers up to $10,000. $1,000 Premium.");
                                System.out.println("2.) Covers up to $100,000. $10,000 Premium.");
                                System.out.println("3.) Covers up to $250,000. $25,000 Premium.");
                                String plan = input.nextLine();
                                if(plan.equals("1")){
                                    premium = 1000;
                                    correct = true;
                                }
                                else if(plan.equals("2")){
                                    premium = 10000;
                                    correct = true;
                                }
                                else if(plan.equals("3")){
                                    premium = 25000;
                                    correct = true;
                                }
                                else{
                                    System.out.println("Enter a correct input option. Please try again.");
                                }
                            }while (correct == false);
                            pStmt = conn.prepareStatement("insert into policy (policy_id, start_date, end_date, premium, year_last_paid) values (?,?,?,?,?)");
                            pStmt.setInt(1,policy_id);
                            pStmt.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                            pStmt.setDate(3, null);
                            pStmt.setInt(4,premium);
                            pStmt.setInt(5,0);
                            pStmt.executeUpdate();
                            pStmt = conn.prepareStatement("insert into buys (customer_id, policy_id) values (?, ?)");
                            pStmt.setInt(1, customer_id);
                            pStmt.setInt(2,policy_id);
                            pStmt.executeUpdate();
                            //prompt the user to choose a type of policy
                            do{
                                System.out.print("What type of policy would you like to buy?\n1.) Car\n2.) House\n3.) Boat\n");
                                choice = input.nextLine();
                                //prompt the user to answer questions about what they want included in the policy
                                if(choice.equals("1")){  
                                    int p = 0; 
                                    boolean correctPassengers = false; 
                                    do{
                                        System.out.print("Do you want your policy to cover all passengers in the vehicle? Y/N\n");
                                        String response = input.nextLine();
                                        if(response.equals("Y")){
                                            p = 1;
                                            correctPassengers = true;
                                        }
                                        else if(response.equals("N")){
                                            p = 0;
                                            correctPassengers = true;
                                        }
                                        else{
                                            System.out.println("Enter a valid response. Please try again.");
                                        }
                                    }while(correctPassengers == false);  
                                    pStmt = conn.prepareStatement("insert into car_policy (policy_id, drivers) values (?,?)");
                                    pStmt.setInt(1,policy_id);
                                    pStmt.setInt(2, p);
                                    pStmt.executeUpdate();
                                    System.out.println("Success. \nPolicy Id: "+policy_id);
                                    correct4 = true;                
                                }
                                else if(choice.equals("2")){
                                    int f = 0; 
                                    boolean correctfire = false; 
                                    do{
                                        System.out.print("Do you want your policy to cover fires? Y/N\n");
                                        String response = input.nextLine();  
                                        if(response.equals("Y")){
                                            f = 1;
                                            correctfire = true;
                                        }
                                        else if(response.equals("N")){
                                            f = 0;
                                            correctfire = true;
                                        }
                                        else{
                                            System.out.println("Enter a valid response. Please try again.");
                                        }
                                    }while(correctfire == false);         
                                    int s = 0; 
                                    boolean correctSmoke = false; 
                                    do{
                                        System.out.print("Do you want your policy to cover smoke? Y/N\n");
                                        String response = input.nextLine();
                                        if(response.equals("Y")){
                                            s = 1;
                                            correctSmoke = true;
                                        }
                                        else if(response.equals("N")){
                                            s = 0;
                                            correctSmoke = true;
                                        }
                                        else{
                                            System.out.println("Enter a valid response. Please try again.");
                                        }
                                    }while(correctSmoke == false);   
                                    int t = 0; 
                                    boolean correctTheft = false; 
                                    do{
                                        System.out.print("Do you want your policy to cover theft? Y/N\n");
                                        String response = input.nextLine();
                                        if(response.equals("Y")){
                                            t = 1;
                                            correctTheft = true;
                                        }
                                        else if(response.equals("N")){
                                            t = 0;
                                            correctTheft = true;
                                        }
                                        else{
                                            System.out.println("Enter a valid response. Please try again.");
                                        }
                                    }while(correctTheft == false);                   
                                    pStmt2 = conn.prepareStatement("insert into house_policy (policy_id, fire, smoke, theft) values (?,?,?,?)");
                                    pStmt2.setInt(1,policy_id);
                                    pStmt2.setInt(2, f);
                                    pStmt2.setInt(3, s);
                                    pStmt2.setInt(4, t);
                                    pStmt2.executeUpdate();
                                    System.out.println("Success. \nPolicy Id: "+policy_id);
                                    correct4 = true;
                                }
                                else if(choice.equals("3")){
                                    int d = 0; 
                                    boolean correctDamage = false; 
                                    do{
                                        System.out.print("Do you want your policy to cover damage? Y/N\n");
                                        String response = input.nextLine();  
                                        if(response.equals("Y")){
                                            d = 1;
                                            correctDamage = true;
                                        }
                                        else if(response.equals("N")){
                                            d = 0;
                                            correctDamage = true;
                                        }
                                        else{
                                            System.out.println("Enter a valid response. Please try again.");
                                        }
                                    }while(correctDamage == false);         
                                    int p = 0; 
                                    boolean correctPassengers = false; 
                                    do{
                                        System.out.print("Do you want your policy to cover all passenegers on the boat? Y/N\n");
                                        String response = input.nextLine();
                                        if(response.equals("Y")){
                                            p = 1;
                                            correctPassengers = true;
                                        }
                                        else if(response.equals("N")){
                                            p = 0;
                                            correctPassengers = true;
                                        }
                                        else{
                                            System.out.println("Enter a valid response. Please try again.");
                                        }
                                    }while(correctPassengers == false);          
                                    pStmt2 = conn.prepareStatement("insert into boat_policy (policy_id, damage, passengers) values (?,?,?)");
                                    pStmt2.setInt(1,policy_id);
                                    pStmt2.setInt(2, d);
                                    pStmt2.setInt(3, p);
                                    pStmt2.executeUpdate();
                                    System.out.println("Success. \nPolicy Id: "+policy_id);
                                    correct4 = true;
                                }
                                else{
                                    System.out.println("Please enter a valid option. Try again.");     
                                }
                            }while(correct4 == false);
                        }
                        else{
                            System.out.println("There is no Customer with the entered Id.");
                        }
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "4":
                    try{
                        //allow a customer to cancel a policy
                        //prompt customer to enter Customer Id
                        int customer_id = getId(1, input);
                        int policy_id = getId(2, input);
                        pStmt = conn.prepareStatement("select * from buys where policy_id = (?) and customer_id = (?)");
                        pStmt.setInt(1, policy_id);
                        pStmt.setInt(2, customer_id);
                        ResultSet rset = pStmt.executeQuery();
                        if(rset.next()){
                            pStmt = conn.prepareStatement("select end_date from policy where policy_id = (?)");
                            pStmt.setInt(1, policy_id);
                            rset = pStmt.executeQuery();
                            if(rset.next()){
                                if(rset.getDate("end_date") != null){
                                    System.out.println("Policy has already been cancelled.");
                                    break;
                                }
                            }
                            //set the current date as the end date of the policy
                            pStmt = conn.prepareStatement("update policy set end_date = (?) where policy_id = (?)");
                            pStmt.setDate(1, new java.sql.Date(System.currentTimeMillis()));
                            pStmt.setInt(2, policy_id);
                            pStmt.executeUpdate();
                            System.out.println("Policy cancellation successful.");
                        }
                        else{
                            System.out.println("There is no Customer and Policy Id of that pairing.");
                        }
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "5":
                    try{
                        //allow customer to pay for a policy
                        //prompt user to enter Customer and Policy Ids
                        int customer_id = getId(1, input);
                        int policy_id = getId(2, input);
                        pStmt = conn.prepareStatement("select * from buys where policy_id = (?) and customer_id = (?)");
                        pStmt.setInt(1, policy_id);
                        pStmt.setInt(2, customer_id);
                        ResultSet rset = pStmt.executeQuery();
                        if(rset.next()){
                            Calendar calndr = Calendar.getInstance();
                            int premium = 0 ;
                            int amount = 0;
                            int years = 0;
                            int last_paid = 0;
                            Date date = new Date();
                            pStmt = conn.prepareStatement("select * from policy where policy_id = (?)");
                            pStmt.setInt(1, policy_id);
                            rset = pStmt.executeQuery();
                            while(rset.next()){
                                premium = rset.getInt("premium");
                                last_paid = rset.getInt("year_last_paid");
                                date = rset.getDate("end_date");
                            }
                            //check if the policy has been cancelled
                            if(date != null){
                                pStmt = conn.prepareStatement("select EXTRACT(YEAR FROM end_date) year from policy where policy_id = (?)");
                                pStmt.setInt(1, policy_id);
                                rset = pStmt.executeQuery();
                                while(rset.next()){
                                    int end_year = rset.getInt("year");
                                    if(last_paid < end_year && last_paid != 0){
                                        years = end_year - last_paid;
                                        amount = years * premium;
                                        break;
                                    }
                                    else if(last_paid == 0){
                                        pStmt2 = conn.prepareStatement("select EXTRACT(YEAR FROM start_date) year from policy where policy_id = (?)");
                                        pStmt2.setInt(1, policy_id);
                                        ResultSet rset2 = pStmt2.executeQuery();
                                        while(rset2.next()){
                                            int start_year = rset2.getInt("year");
                                            years = end_year - start_year;
                                            if(years == 0) amount = premium;
                                            else amount = years * premium;
                                        }
                                    }
                                    else if(last_paid == end_year){
                                        System.out.println("This policy has been cancelled, and there is no outstanding balance.");
                                        break;
                                    }
                                }
                            }
                            //determine the amount of money due
                            else if(last_paid == 0){
                                pStmt2 = conn.prepareStatement("select EXTRACT(YEAR FROM start_date) year from policy where policy_id = (?)");
                                pStmt2.setInt(1, policy_id);
                                ResultSet rset2 = pStmt2.executeQuery();
                                if(rset2.next()){
                                    int first = rset2.getInt("year");
                                    years = calndr.get(Calendar.YEAR) - first;
                                    if(years == 0) amount = premium;
                                    else amount = years * premium;
                                }
                            }
                            else if(last_paid < calndr.get(Calendar.YEAR)){
                                years = calndr.get(Calendar.YEAR) - last_paid;
                                amount = years * premium;
                            }
                            else{
                                System.out.println("No payments due for this policy.");
                                break;
                            } 
                            if(amount != 0){
                                System.out.println("Balance due is $"+amount+". You must pay it in full now.");
                                int pay_id = (int)(Math.random() * (999999999 - 1 + 1) + 1);
                                pStmt = conn.prepareStatement("select pay_id from payment order by pay_id asc");
                                rset = pStmt.executeQuery();
                                while(rset.next()){
                                    if(rset.getInt("pay_id") == pay_id){
                                        pay_id++;
                                    }
                                }
                                pStmt = conn.prepareStatement("insert into payment (pay_id, policy_id, year, total, first_name, last_name) values (?,?,?,?,?,?)");
                                pStmt.setInt(1, pay_id);
                                pStmt.setInt(2, policy_id);
                                pStmt.setInt(3, calndr.get(Calendar.YEAR));
                                pStmt.setInt(4, premium * years);
                                System.out.print("Enter first name: ");
                                String first = input.nextLine();
                                System.out.print("Enter last name: ");
                                String last = input.nextLine();
                                pStmt.setString(5, first);
                                pStmt.setString(6, last);
                                pStmt.executeUpdate();
                                pStmt = conn.prepareStatement("insert into pays (pay_id, customer_id) values (?, ?)");
                                pStmt.setInt(1, pay_id);
                                pStmt.setInt(2, customer_id);
                                pStmt.executeUpdate();
                                //call method to get the customer's payment method
                                payMethod(pay_id, pStmt, input, conn); 
                                pStmt = conn.prepareStatement("update policy set year_last_paid = (?) where policy_id = (?)");
                                pStmt.setInt(1, calndr.get(Calendar.YEAR));
                                pStmt.setInt(2, policy_id);
                                pStmt.executeUpdate();     
                                System.out.println("Successfully paid.\nPayment Id: "+pay_id);
                            }
                        }
                        else{
                            System.out.println("There is no Customer and Policy Id of that pairing.");
                        }
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "6":
                    try{
                        //allow customer to make a claim
                        //prompt user to enter Customer and Policy Ids
                        int customer_id = getId(1, input);
                        int policy_id = getId(2, input);
                        pStmt = conn.prepareStatement("select * from buys where policy_id = (?) and customer_id = (?)");
                        pStmt.setInt(1, policy_id);
                        pStmt.setInt(2, customer_id);
                        ResultSet rset = pStmt.executeQuery();
                        //ensure the entered policy has not be cancelled
                        if(rset.next()){
                            pStmt = conn.prepareStatement("select end_date from policy where policy_id = (?)");
                            pStmt.setInt(1, policy_id);
                            ResultSet rset2 = pStmt.executeQuery();
                            if(rset2.next()){
                                if(rset2.getDate("end_date") != null){
                                    System.out.println("This policy has been cancelled.");
                                    break;
                                }
                            }
                            //prompt the user to enter the Item Id to which the claim refers
                            //prompt user to enter details of the claim
                            int item_id = getId(3, input);
                            pStmt = conn.prepareStatement("select * from insures where policy_id = (?) and item_id = (?)");
                            pStmt.setInt(1, policy_id);
                            pStmt.setInt(2, item_id);
                            rset = pStmt.executeQuery();
                            int claim_id = 0;
                            if(rset.next()){
                                String d = getString(7, input);
                                claim_id = (int)(Math.random() * (999999999 - 1 + 1) + 1);
                                pStmt = conn.prepareStatement("select claim_id from claim order by claim_id asc");
                                rset = pStmt.executeQuery();
                                while(rset.next()){
                                    if(rset.getInt("claim_id") == claim_id){
                                        claim_id++;
                                    }
                                }
                                pStmt = conn.prepareStatement("insert into claim (claim_id, issue_date, description, company_pay) values (?, ?, ?, ?)");
                                pStmt.setInt(1, claim_id);
                                pStmt.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                                boolean x = false;
                                int amount = 0;
                                do{
                                    try{
                                        System.out.print("Enter Claim Amount (Up to $10,000). Customer pays 25% of amount. Company pays the remaining 75%: ");
                                        amount = input.nextInt();
                                        input.nextLine();
                                        if(amount > 0 && amount <= 10000){
                                            x = true;
                                        }
                                        else System.out.println("Enter a valid input. Please try again.");
                                    }
                                    catch(InputMismatchException se){
                                        System.out.println("Enter a valid input. Please try again.");
                                        input.next();
                                    }
                                    
                                }while (x == false);
                                pStmt.setString(3, d);
                                pStmt.setDouble(4,amount * .75);
                                pStmt.executeUpdate();
                                //calculate the price of the claim for the customer
                                System.out.println("Balance due is $"+amount * .25+". You must pay it in full now.");
                                int pay_id = (int)(Math.random() * (999999999 - 1 + 1) + 1);
                                pStmt = conn.prepareStatement("select pay_id from payment order by pay_id asc");
                                rset = pStmt.executeQuery();
                                while(rset.next()){
                                    if(rset.getInt("pay_id") == pay_id){
                                        pay_id++;
                                    }
                                }
                                pStmt = conn.prepareStatement("insert into payment (pay_id, policy_id, year, total, first_name, last_name) values (?,?,?,?,?,?)");
                                pStmt.setInt(1, pay_id);
                                pStmt.setInt(2, policy_id);
                                Calendar calndr = Calendar.getInstance();
                                pStmt.setInt(3, calndr.get(Calendar.YEAR));
                                pStmt.setDouble(4, amount * .25);
                                System.out.print("Enter first name: ");
                                String first = input.nextLine();
                                System.out.print("Enter last name: ");
                                String last = input.nextLine();
                                pStmt.setString(5, first);
                                pStmt.setString(6, last);
                                pStmt.executeUpdate();
                                pStmt = conn.prepareStatement("insert into pays (pay_id, customer_id) values (?, ?)");
                                pStmt.setInt(1, pay_id);
                                pStmt.setInt(2, customer_id);
                                pStmt.executeUpdate();
                                //prompt user to enter pay method for the claim
                                payMethod(pay_id, pStmt, input, conn);
                                pStmt = conn.prepareStatement("insert into submits (claim_id, customer_id) values (?, ?)");
                                pStmt.setInt(1, claim_id);
                                pStmt.setInt(2, customer_id);
                                pStmt.executeUpdate();
                                pStmt = conn.prepareStatement("insert into belongs_to (policy_id, claim_id) values (?, ?)");
                                pStmt.setInt(1, policy_id);
                                pStmt.setInt(2, claim_id);
                                pStmt.executeUpdate();
                                pStmt = conn.prepareStatement("insert into refers_to (item_id, claim_id) values (?, ?)");
                                pStmt.setInt(1, item_id);
                                pStmt.setInt(2, claim_id);
                                pStmt.executeUpdate();
                                pStmt = conn.prepareStatement("select MAX(adjuster_id) from adjuster");
                                rset = pStmt.executeQuery();
                                while(rset.next()){
                                    int adjuster_id = rset.getInt("MAX(adjuster_id)");
                                    pStmt = conn.prepareStatement("insert into manages (claim_id, adjuster_id) values (?, ?)");
                                    pStmt.setInt(1, claim_id);
                                    pStmt.setInt(2, adjuster_id);
                                    pStmt.executeUpdate();
                                }
                            }
                            else{
                                System.out.println("Item Id does not exist under that policy.");
                                break;
                            }
                            System.out.println("Success.\nClaim Id: "+claim_id);
                        }
                        else{
                            System.out.println("There is no Customer and Policy Id of that pairing.");
                        }
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "7":
                    try{
                        //allow customer to add an item to a policy
                        //prompt user to enter policy id
                        int customer_id = getId(1, input);
                        int policy_id = getId(2, input);
                        pStmt = conn.prepareStatement("select * from buys where policy_id = (?) and customer_id = (?)");
                        pStmt.setInt(1, policy_id);
                        pStmt.setInt(2, customer_id);
                        ResultSet rset = pStmt.executeQuery();
                        //ensure the entered policy has not be cancelled
                        if(rset.next()){
                            boolean x = false;
                            pStmt = conn.prepareStatement("select end_date from policy where policy_id = (?)");
                            pStmt.setInt(1, policy_id);
                            rset = pStmt.executeQuery();
                            if(rset.next()){
                                if(rset.getDate("end_date") != null){
                                    System.out.println("This policy has been cancelled.");
                                    break;
                                }
                            }     
                            pStmt1 = conn.prepareStatement("select * from car_policy where policy_id = (?)");
                            pStmt2 = conn.prepareStatement("select * from boat_policy where policy_id = (?)");
                            pStmt3 = conn.prepareStatement("select * from house_policy where policy_id = (?)");
                            pStmt1.setInt(1, policy_id);
                            pStmt2.setInt(1, policy_id);
                            pStmt3.setInt(1, policy_id);
                            ResultSet rset1 = pStmt1.executeQuery();
                            ResultSet rset2 = pStmt2.executeQuery();
                            ResultSet rset3 = pStmt3.executeQuery();
                            int item_id = (int)(Math.random() * (999999999 - 1 + 1) + 1);
                            pStmt = conn.prepareStatement("select item_id from item order by item_id asc");
                            rset = pStmt.executeQuery();
                            while(rset.next()){
                                if(rset.getInt("item_id") == item_id){
                                    item_id++;
                                }
                            }
                            //prompt customer to enter the details about the item
                            if(rset1.next()){
                                x = false;
                                String vin ="";
                                do{
                                    System.out.println("Policy "+policy_id+" is a car policy.\nEnter the VIN of the car to be insured (17 characters): ");
                                    vin = input.nextLine();
                                    if(vin.length() != 17){
                                        System.out.println("Enter valid input. Please try again.");
                                    }
                                    else x = true;
                                }while (x == false);
                                String model = getString(8, input);
                                pStmt1 = conn.prepareStatement("insert into car_item (item_id, vin_num, model) values (?, ?, ?)");
                                pStmt1.setInt(1, item_id);
                                pStmt1.setString(2, vin);
                                pStmt1.setString(3, model);
                                pStmt1.executeUpdate();
                            }
                            else if(rset2.next()){
                                pStmt2 = conn.prepareStatement("insert into item (item_id) values (?)");
                                pStmt2.setInt(1, item_id);
                                pStmt2.executeUpdate();
                                System.out.println("Policy "+policy_id+" is a boat policy.");
                                String name = getString(9, input);
                                x = false;
                                int c = 0;
                                do{
                                    try{
                                        System.out.println("Enter the class of the boat to be insured (1, 2, or 3): ");
                                        c = input.nextInt();
                                        input.nextLine();
                                        if(c != 1 || c != 2 || c != 3){
                                            System.out.println("Enter a valid input. Please try again.");
                                        }
                                        else x = true;
                                    }
                                    catch(InputMismatchException se){
                                        System.out.println("Enter a valid input. Please try again.");
                                        input.next();
                                    }
                                    
                                }while (x == false);
                                pStmt2 = conn.prepareStatement("insert into boat_item (item_id, name, class) values (?, ?, ?)");
                                pStmt2.setInt(1, item_id);
                                pStmt2.setString(2, name);
                                pStmt2.setInt(3, c);
                                pStmt2.executeUpdate();
                            }
                            else if(rset3.next()){
                                pStmt3 = conn.prepareStatement("insert into item (item_id) values (?)");
                                pStmt3.setInt(1, item_id);
                                pStmt3.executeUpdate();
                                System.out.println("Policy "+policy_id+" is a house policy.");
                                x = false;
                                int num = 0;
                                do{
                                    try{
                                        System.out.println("Enter the address number of the house to be insured: ");
                                        num = input.nextInt();
                                        input.nextLine();
                                        x = true;
                                    }
                                    catch(InputMismatchException se){
                                        System.out.println("Enter a valid input. Please try again.");
                                        input.next();
                                    }
                                }while (x == false);
                                String street = getString(3, input);
                                String city = getString(4, input);
                                pStmt3 = conn.prepareStatement("insert into house_item (item_id, num, street, city) values (?,?,?,?)");
                                pStmt3.setInt(1, item_id);
                                pStmt3.setInt(2, num);
                                pStmt3.setString(3, street);
                                pStmt3.setString(4, city);
                                pStmt3.executeUpdate();
                            }
                            else{
                                System.out.println("Entered Policy Id does not exist.");
                                break;
                            }
                            pStmt1 = conn.prepareStatement("insert into insures (item_id, policy_id) values (?, ?)");
                            pStmt1.setInt(1, item_id);
                            pStmt1.setInt(2, policy_id);
                            pStmt1.executeUpdate();
                            System.out.println("Success. \nItem Id: "+item_id);
                        }
                        else{
                            System.out.println("There is no Customer and Policy Id of that pairing.");
                        }
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "8":
                    try{
                        //allow customer to add an item to a policy
                        //prompt user to enter policy id
                        System.out.println("Policy must be a car or boat policy.");
                        int customer_id = getId(1, input);
                        int policy_id = getId(2, input);
                        pStmt = conn.prepareStatement("select * from buys where policy_id = (?) and customer_id = (?)");
                        pStmt.setInt(1, policy_id);
                        pStmt.setInt(2, customer_id);
                        ResultSet rset = pStmt.executeQuery();
                        //ensure the entered policy has not be cancelled
                        if(rset.next()){
                            pStmt = conn.prepareStatement("select end_date from policy where policy_id = (?)");
                            pStmt.setInt(1, policy_id);
                            rset = pStmt.executeQuery();
                            if(rset.next()){
                                if(rset.getDate("end_date") != null){
                                    System.out.println("This policy has been cancelled.");
                                    break;
                                }
                            }
                            pStmt1 = conn.prepareStatement("select * from car_policy where policy_id = (?)");
                            pStmt2 = conn.prepareStatement("select * from boat_policy where policy_id = (?)");
                            pStmt1.setInt(1, policy_id);
                            pStmt2.setInt(1, policy_id);
                            ResultSet rset1 = pStmt1.executeQuery();
                            ResultSet rset2 = pStmt2.executeQuery();
                            int person_id = (int)(Math.random() * (999999999 - 1 + 1) + 1);
                            pStmt = conn.prepareStatement("select person_id from person order by person_id asc");
                            rset = pStmt.executeQuery();
                            while(rset.next()){
                                if(rset.getInt("person_id") == person_id){
                                    person_id++;
                                }
                            }
                            //make sure the policy covers people
                            //prompt customer to enter details about the person
                            if(rset1.next()){
                                if(rset1.getInt("drivers") == 1){
                                    String first = getString(1, input);
                                    String last = getString(2, input);
                                    pStmt = conn.prepareStatement("insert into person (person_id, first_name, last_name) values (?, ?, ?)");
                                    pStmt.setInt(1,person_id);
                                    pStmt.setString(2,first);
                                    pStmt.setString(3,last);
                                    pStmt.executeUpdate();
                                }
                                else{
                                    System.out.println("This car policy does not cover drivers.");
                                    break;
                                }
                            }
                            else if(rset2.next()){
                                if(rset1.getInt("passengers") == 1){
                                    String first = getString(1, input);
                                    String last = getString(2, input);
                                    pStmt2 = conn.prepareStatement("insert into person (person_id, first_name, last_name) values (?, ?, ?)");
                                    pStmt2.setInt(1,person_id);
                                    pStmt2.setString(2,first);
                                    pStmt2.setString(3,last);
                                    pStmt2.executeUpdate();
                                }
                                else{
                                    System.out.println("This boat policy does not cover passengers.");
                                    break;
                                }
                            }
                            else{
                                System.out.println("Policy not found.");
                                break;
                            }
                            pStmt = conn.prepareStatement("insert into covers (person_id, policy_id) values (?, ?)");
                            pStmt.setInt(1,person_id);
                            pStmt.setInt(2,policy_id);
                            pStmt.executeUpdate();
                            System.out.println("Success. \nPerson Id: "+person_id);
                        }
                        else{
                            System.out.println("There is no Customer and Policy Id of that pairing.");
                        }
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "9":
                    try{
                        //allow customer to remove an item to a policy
                        //prompt user to enter Customer Id, Policy Id, and Item Id
                        int customer_id = getId(1, input);
                        int policy_id = getId(2, input);
                        int item_id = getId(3, input);
                        pStmt = conn.prepareStatement("select * from buys natural join insures where customer_id = (?) and policy_id = (?) and item_id = (?)");
                        pStmt.setInt(1, customer_id);
                        pStmt.setInt(2, policy_id);
                        pStmt.setInt(3, item_id);
                        ResultSet rset = pStmt.executeQuery();
                        if(rset.next()){
                            //ensure the entered policy has not be cancelled
                            pStmt = conn.prepareStatement("select end_date from policy where policy_id = (?)");
                            pStmt.setInt(1, policy_id);
                            ResultSet rset2 = pStmt.executeQuery();
                            if(rset2.next()){
                                if(rset2.getDate("end_date") != null){
                                    System.out.println("This policy has been cancelled.");
                                    break;
                                }
                            }
                            pStmt = conn.prepareStatement("delete from item where item_id = (?)");
                            pStmt.setInt(1, item_id);
                            pStmt.executeUpdate();
                            System.out.println("Item removed successfully.");
                        }
                        else{
                            System.out.println("There is no item with that Item Id that belongs to that policy and customer.");
                        }
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "10":
                    try{
                        //allow customer to remove a person from a policy
                        //prompt user to enter Customer Id, Policy Id, and Person Id
                        int customer_id = getId(1, input);
                        int policy_id = getId(2, input);
                        int person_id = getId(4, input);
                        pStmt = conn.prepareStatement("select * from buys natural join covers where customer_id = (?) and policy_id = (?) and person_id = (?)");
                        pStmt.setInt(1, customer_id);
                        pStmt.setInt(2, policy_id);
                        pStmt.setInt(3, person_id);
                        ResultSet rset = pStmt.executeQuery();
                        if(rset.next()){
                             //ensure the entered policy has not be cancelled
                             pStmt = conn.prepareStatement("select end_date from policy where policy_id = (?)");
                             pStmt.setInt(1, policy_id);
                             ResultSet rset2 = pStmt.executeQuery();
                             if(rset2.next()){
                                 if(rset2.getDate("end_date") != null){
                                     System.out.println("This policy has been cancelled.");
                                     break;
                                 }
                             }
                            pStmt = conn.prepareStatement("delete from person where person_id = (?)");
                            pStmt.setInt(1, person_id);
                            pStmt.executeUpdate();
                            System.out.println("Person removed successfully.");
                        }
                        else{
                            System.out.println("There is no person with that Person Id that belongss to that policy and customer.");
                        }
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "11":
                    //leave Customer interface and return to main menu
                    correct1 = true;
                    break;
                default:
                    //inform customer that they entered an invalid option and ask them to try again
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }while (correct1 == false);
        return;
    }

/************************************************************************************************************************************************************************/
   
    public static void agent(Connection conn, PreparedStatement pStmt, Scanner input){
        boolean correct2 = false;
        System.out.println();
        System.out.println("Entering the Agent interface.");
        //display operation menu and prompt agent to pick an option
        do{
            System.out.println();
            System.out.println("Select an operation: ");
            System.out.println("1.) View agent's associated customers.");
            System.out.println("2.) Identify customers with overdue bills.");
            System.out.println("3.) View revenue generated by agent.");
            System.out.println("4.) View customers with claims that have not been serviced yet.");
            System.out.println("5.) Exit Agent interface.");
            String choice = input.nextLine();
            switch (choice){
                case "1":
                    try{
                        //view all the customers associated with a particular agent
                        //prompt agent to enter Agent Id
                        int agent_id = getId(5, input);
                        pStmt = conn.prepareStatement("select * from agent where agent_id = (?)");
                        pStmt.setInt(1,agent_id);
                        ResultSet rset = pStmt.executeQuery();
                        if(!rset.next()){
                            System.out.println("Agent Id does not exist.");
                            break;
                        }
                        pStmt = conn.prepareStatement("select * from advises natural join customer where agent_id = (?)");
                        pStmt.setInt(1, agent_id);
                        rset = pStmt.executeQuery();
                        int i = 0;
                        while(rset.next()){
                            //display the details of each customer
                            i++;
                            int customer_id = rset.getInt("customer_id");
                            String first_name = rset.getString("first_name");
                            String last_name = rset.getString("last_name");
                            System.out.println(i+".) Customer ID: "+customer_id+"\tName: "+first_name+" "+last_name);
                        }
                        if(i == 0) System.out.println("No customers associated with this agent.");
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "2":
                    try{
                        //identify customers with overdue bills
                        //prompt agent to enter Agent Id
                        int agent_id = getId(5, input);
                        pStmt = conn.prepareStatement("select * from agent where agent_id = (?)");
                        pStmt.setInt(1,agent_id);
                        ResultSet rset = pStmt.executeQuery();
                        if(!rset.next()){
                            System.out.println("Agent Id does not exist.");
                            break;
                        }
                        Calendar calndr = Calendar.getInstance();
                        pStmt = conn.prepareStatement("select * from policy natural join buys natural join customer");
                        rset = pStmt.executeQuery();
                        int i = 0;
                        while(rset.next()){
                            int amount = 0;
                            int years = 0;
                            int premium = rset.getInt("premium");
                            int last_paid = rset.getInt("year_last_paid");
                            Date date = rset.getDate("end_date");
                            int policy_id = rset.getInt("policy_id");
                            int customer_id = rset.getInt("customer_id");
                            String first_name = rset.getString("first_name");
                            String last_name = rset.getString("last_name");
                            //check if the policy has been cancelled
                            if(date != null){
                                pStmt = conn.prepareStatement("select EXTRACT(YEAR FROM end_date) year from policy where policy_id = (?)");
                                pStmt.setInt(1, policy_id);
                                ResultSet rset2 = pStmt.executeQuery();
                                while(rset2.next()){
                                    int end_year = rset2.getInt("year");
                                    if(last_paid < end_year && last_paid != 0){
                                        years = end_year - last_paid;
                                        amount = years * premium;
                                        break;
                                    }
                                    else if(last_paid == 0){
                                        pStmt = conn.prepareStatement("select EXTRACT(YEAR FROM start_date) year from policy where policy_id = (?)");
                                        pStmt.setInt(1, policy_id);
                                        ResultSet rset3 = pStmt.executeQuery();
                                        while(rset3.next()){
                                            int start_year = rset3.getInt("year");
                                            years = end_year - start_year;
                                            if(years == 0) amount = premium;
                                            else amount = years * premium;
                                        }
                                    }
                                }
                            }
                            //determine the amount of money due
                            else if(last_paid == 0){
                                pStmt = conn.prepareStatement("select EXTRACT(YEAR FROM start_date) year from policy where policy_id = (?)");
                                pStmt.setInt(1, policy_id);
                                ResultSet rset2 = pStmt.executeQuery();
                                if(rset2.next()){
                                    int first = rset2.getInt("year");
                                    years = calndr.get(Calendar.YEAR) - first;
                                    if(years == 0) amount = premium;
                                    else amount = years * premium;
                                }
                            }
                            else if(last_paid < calndr.get(Calendar.YEAR)){
                                years = calndr.get(Calendar.YEAR) - last_paid;
                                amount = years * premium;
                            }
                            if(amount != 0){
                                i++;
                                System.out.println(i+".) Customer ID: "+customer_id+"\t\tName: "+first_name+" "+last_name+"\t\tPolicy ID: "+policy_id+"\t\tUnpaid Amount: "+amount);
                            }
                        }  
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "3":
                    try{
                        //view revenue generated by the agent
                        //prompt agent to enter agent id
                        int agent_id = getId(5, input);
                        pStmt = conn.prepareStatement("select * from agent where agent_id = (?)");
                        pStmt.setInt(1,agent_id);
                        ResultSet rset = pStmt.executeQuery();
                        if(!rset.next()){
                            System.out.println("Agent Id does not exist.");
                            break;
                        }
                        pStmt = conn.prepareStatement("select sum(total) total from advises natural join (pays natural join payment) where agent_id = (?)");
                        pStmt.setInt(1, agent_id);
                        rset = pStmt.executeQuery();
                        int i = 0;
                        while(rset.next()){
                            i++;
                            //display the revenue
                            double total = rset.getDouble("total");
                            System.out.println("Revenue: $"+total);
                        }
                        if(i==0) System.out.println("No profits from this agent.");
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "4":
                    try{
                        //view customers with unserviced claims
                        //propmt agent to enter Agent Id
                        int agent_id = getId(5, input);
                        pStmt = conn.prepareStatement("select * from agent where agent_id = (?)");
                        pStmt.setInt(1,agent_id);
                        ResultSet rset = pStmt.executeQuery();
                        if(!rset.next()){
                            System.out.println("Agent Id does not exist.");
                            break;
                        }
                        pStmt = conn.prepareStatement("select service_date, customer_id, claim_id, first_name, last_name from (claim natural join submits) natural join customer where service_date is NULL");
                        rset = pStmt.executeQuery();
                        int i = 0;
                        while(rset.next()){
                            //display the information of the customers with overdues claims
                            i++;
                            int customer_id = rset.getInt("customer_id");
                            int claim_id = rset.getInt("claim_id");
                            String first = rset.getString("first_name");
                            String last = rset.getString("last_name");
                            System.out.println(i+".) Customer Id: "+customer_id+"\t\tClaim Id: "+claim_id+"\t\tName: "+first+" "+last);
                        }
                        if(i == 0) System.out.println("No customers with claims that have not been serviced.");
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "5":
                    //leave the Agent interface and return to the main menu
                    correct2 = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }while (correct2 == false);
        return; 
    }

/************************************************************************************************************************************************************************/

    public static void adjuster(Connection conn, PreparedStatement pStmt, Scanner input){
        boolean correct3 = false;
        System.out.println();
        System.out.println("Entering the Adjuster interface.");
        //display menu options and prompt the user to choose an option
        do{
            System.out.println();
            System.out.println("Select an operation: ");
            System.out.println("1.) View adjuster's associated claims.");
            System.out.println("2.) View claims that have not been serviced yet.");
            System.out.println("3.) Service and outsource a claim.");
            System.out.println("4.) Exit Adjuster interface.");
            String choice = input.nextLine();
            switch (choice){
                case "1":
                    try{
                        //view claims associated with an adjuster
                        //prompt adjuster ot enter Adjuster Id
                        int adjuster_id = getId(6, input);
                        pStmt = conn.prepareStatement("select * from adjuster where adjuster_id = (?)");
                        pStmt.setInt(1,adjuster_id);
                        ResultSet rset = pStmt.executeQuery();
                        if(!rset.next()){
                            System.out.println("Adjuster Id does not exist.");
                            break;
                        }
                        pStmt = conn.prepareStatement("select * from ((manages natural join claim) natural join submits) natural join customer where adjuster_id = (?)");
                        pStmt.setInt(1, adjuster_id);
                        rset = pStmt.executeQuery();
                        int i = 0;
                        while(rset.next()){
                            i++;
                            //display information about all of the associated claims
                            int claim_id = rset.getInt("claim_id");
                            int customer_id = rset.getInt("customer_id"); 
                            String first = rset.getString("first_name"); 
                            String last = rset.getString("last_name");  
                            Date issue_date = rset.getDate("issue_date");    
                            String d = rset.getString("description");   
                            int pay = rset.getInt("company_pay");                  
                            System.out.println(i+".) Claim ID: "+claim_id+"\t\tCustomer ID: "+customer_id+"\t\tName: "+first+" "+last+"\t\tClaim Description: "+d+"\t\tCompany Payment: $"+pay+"\t\tIssue Date: "+issue_date);
                        }
                        if(i == 0) System.out.println("No claims associated with this Adjuster Id.");
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "2":
                    try{
                        //view information about all of the unserviced claims
                        //prompt adjuster ot enter Adjuster Id
                        int adjuster_id = getId(6, input);
                        pStmt = conn.prepareStatement("select * from adjuster where adjuster_id = (?)");
                        pStmt.setInt(1,adjuster_id);
                        ResultSet rset = pStmt.executeQuery();
                        if(!rset.next()){
                            System.out.println("Adjuster Id does not exist.");
                            break;
                        }
                        pStmt = conn.prepareStatement("select * from adjuster where adjuster_id = (?)");
                        pStmt.setInt(1,adjuster_id);
                        rset = pStmt.executeQuery();
                        if(!rset.next()){
                            System.out.println("Adjuster Id does not exist.");
                            break;
                        }
                        pStmt = conn.prepareStatement("select * from claim where service_date is NULL");
                        rset = pStmt.executeQuery();
                        int i = 0;
                        while(rset.next()){
                            i++;
                            //display information about the unserviced claims
                            int claim_id = rset.getInt("claim_id");
                            Date issue = rset.getDate("issue_date");
                            String d = rset.getString("description");
                            int pay = rset.getInt("company_pay");
                            System.out.println(i+".) Claim Id: "+claim_id+"\t\tIssue Date: "+issue+"\t\tDescription: "+d+"\t\tCompany Payment: "+pay);
                        }
                        if(i == 0) System.out.println("No claims needing service.");
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "3":
                    try{
                        //allow adjuster to service and outsource a claim
                        //prompt adjuster to enter Adjuster Id
                        int adjuster_id = getId(6, input);
                        int claim_id =  getId(7, input);
                        pStmt = conn.prepareStatement("select * from manages where claim_id = (?) and adjuster_id = (?)");
                        pStmt.setInt(1, claim_id);
                        pStmt.setInt(2, adjuster_id);
                        ResultSet rset = pStmt.executeQuery();
                        int outsource_id = 0;
                        if(rset.next()){
                            boolean x = false;
                            //choose to outsource the claim to a new company or one alread existing in the database
                            do{
                                System.out.print("Do you want to outsource to a company that already exists in the database? Y/N: ");
                                String out = input.nextLine();
                                if(out.equals("Y")){
                                    outsource_id = getId(8, input);
                                    pStmt = conn.prepareStatement("select * from outsource where outsource_id = (?)");
                                    pStmt.setInt(1,outsource_id);
                                    rset = pStmt.executeQuery();
                                    if(!rset.next()){
                                        System.out.println("Outsource Id does not exist.");
                                    }
                                    x = true;
                                }
                                else if(out.equals("N")){
                                    String name = getString(5, input);
                                    String type = getString(6, input);
                                    String num = "";
                                    double phone = 0;
                                    boolean y  = false; 
                                    do{
                                        try{
                                            System.out.print("Enter the phone number of the company (no spaces or dashes, just the digits): ");
                                            num = input.nextLine();
                                            if(num.length() != 10){
                                                System.out.println("Enter a valid input. Please try again.");
                                            }
                                            else{
                                                phone = Double.parseDouble(num);  
                                                y = true;
                                            }
                                        }
                                        catch(NumberFormatException se){
                                            System.out.println("Enter a valid input. Please try again.");
                                        }  
                                    }while (y == false);
                                    outsource_id = (int)(Math.random() * (999999999 - 1 + 1) + 1);
                                    pStmt = conn.prepareStatement("select outsource_id from outsource order by outsource_id asc");
                                    rset = pStmt.executeQuery();
                                    while(rset.next()){
                                        if(rset.getInt("outsource_id") == outsource_id){
                                            outsource_id++;
                                        }
                                    }
                                    pStmt = conn.prepareStatement("insert into outsource (outsource_id, name, type, phone) values (?, ?, ?, ?)");
                                    pStmt.setInt(1, outsource_id);
                                    pStmt.setString(2, name);
                                    pStmt.setString(3, type);
                                    pStmt.setDouble(4,phone);
                                    pStmt.executeUpdate();
                                    pStmt = conn.prepareStatement("insert into outsources (claim_id, outsource_id) values (?, ?)");
                                    pStmt.setInt(1, claim_id);
                                    pStmt.setInt(2, outsource_id);
                                    pStmt.executeUpdate();
                                    pStmt = conn.prepareStatement("update claim set service_date = (?) where claim_id = (?)");
                                    pStmt.setDate(1, new java.sql.Date(System.currentTimeMillis()));
                                    pStmt.setInt(2, claim_id);
                                    pStmt.executeUpdate();
                                    System.out.println("Success");
                                    x = true;
                                }
                                else System.out.println("Invalid choice. Please try again.");
                            }while(x == false);
                        }
                        else{
                            System.out.println("There is no Claim and Adjuster Id of that pairing.");
                        }
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "4":
                    //exit Adjuster interface and return to main menu
                    correct3 = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }while (correct3 == false);
        return;
    }

/************************************************************************************************************************************************************************/

    public static void corporate(Connection conn, PreparedStatement pStmt, Scanner input){
        boolean correct4 = false;
        System.out.println();
        System.out.println("Entering the Corporate Management interface.");
        //display menu and prompt user to pick an option
        do{
            System.out.println();
            System.out.println("Select an operation: ");
            System.out.println("1.) View total revenue.");
            System.out.println("2.) View total company payment of claims.");
            System.out.println("3.) View a ranking of customers from most to least profitable.");
            System.out.println("4.) View a ranking of most to least profitable years.");
            System.out.println("5.) View profits based on policy type.");
            System.out.println("6.) View rollup of profits per customer per policy per year.");
            System.out.println("7.) Exit Corporate Management interface.");
            String choice = input.nextLine();
            switch (choice){
                case "1":
                    try{
                        //view total revenue
                        //sum the totals of all the payments in the payment relation
                        pStmt = conn.prepareStatement("select sum(total) from payment");
                        ResultSet rset = pStmt.executeQuery();
                        if(rset.next()) System.out.println("Total revenue: $"+rset.getDouble("sum(total)"));
                        else System.out.println("No revenue yet.");
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "2":
                    try{
                        //view total company payment of claims
                        //sum all of the company payments for each of the claims in claim
                        pStmt = conn.prepareStatement("select sum(company_pay) from claim");
                        ResultSet rset = pStmt.executeQuery();
                        if(rset.next()) System.out.println("Total payment: $"+rset.getDouble("sum(company_pay)"));
                        else System.out.println("No payments yet.");
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "3":
                    try{
                        //view ranking of customers from most to least profitable
                        //sum all of the payments for each customer and rank them from greatest to least amounts
                        pStmt = conn.prepareStatement("select customer_id, sum(total) total, rank() over (order by sum(total) desc) rank from payment natural join pays group by customer_id order by rank");
                        ResultSet rset = pStmt.executeQuery();
                        if(rset != null){
                            while(rset.next()){
                                System.out.println("Customer Id: "+rset.getInt("customer_id")+"\t\tRevenue: $"+rset.getDouble("total")+"\t\tRank: "+rset.getInt("rank"));
                            }
                        }
                        else System.out.println("No profits yet.");
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "4":
                    try{
                        //view ranking of most to least profitable years
                        //sum the payments for each year and rank them from greatest to least amounts
                        pStmt = conn.prepareStatement("select year, sum(total) total, rank() over (order by sum(total) desc) rank from payment group by year order by rank");
                        ResultSet rset = pStmt.executeQuery();
                        if(rset != null){
                            while(rset.next()){
                                System.out.println("Year: "+rset.getInt("year")+"\t\tRevenue: $"+rset.getDouble("total")+"\t\tRank: "+rset.getInt("rank"));
                            }
                        }
                        else System.out.println("No profits yet.");
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "5":
                    try{
                        //view profits based on policy type
                        boolean correct = false;
                        String type = "";
                        do{
                            //choose type of policy
                            System.out.println("Select the type of policy. \n1.) Car\n2.) Boat\n3.) House");
                            String plan = input.nextLine();
                            if(plan.equals("1")){
                                pStmt = conn.prepareStatement("select policy_id from car_policy");
                                type = "car";
                                correct = true;
                            }
                            else if(plan.equals("2")){
                                pStmt = conn.prepareStatement("select policy_id from boat_policy");
                                type = "boat";
                                correct = true;
                            }
                            else if(plan.equals("3")){
                                pStmt = conn.prepareStatement("select policy_id from house_policy");
                                type = "house";
                                correct = true;
                            }
                            else{
                                System.out.println("Enter a correct input option. Please try again.");
                            }
                        }while (correct == false);
                        ResultSet rset = pStmt.executeQuery();
                        double total = 0;
                        int i = 0;
                        while(rset.next()){
                            i++;
                            //get the amount of payment for each policy of the selected type and add it to the total for that type
                            PreparedStatement pStmt2 = conn.prepareStatement("select total from payment where policy_id = (?)");
                            pStmt2.setInt(1, rset.getInt("policy_id"));
                            ResultSet rset2 = pStmt2.executeQuery();
                            while(rset2.next()) total = total + rset2.getInt("total");
                        } 
                        if(i == 0) System.out.println("No profits yet.");
                        else System.out.println("Total profits from "+type+" policies are $"+total+".");
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "6":
                    try{
                        //view rollup of profits per customer per policy per year
                        pStmt = conn.prepareStatement("select customer_id, policy_id, year, sum(total) from pays natural join payment group by rollup (customer_id, policy_id, year)");
                        ResultSet rset = pStmt.executeQuery();
                        if(rset != null){
                            while(rset.next()){
                                System.out.println("Customer Id: "+rset.getString("customer_id")+"\t\tPolicy Id: "+rset.getString("policy_id")+"\t\tYear: "+rset.getString("year")+"\t\tRevenue: "+rset.getInt("sum(total)"));
                            }
                        }
                        else System.out.println("No profits yet.");
                    }
                    catch(Exception se){
                        System.out.println(se);
                    }
                    break;
                case "7":
                    //exit the Corporate Management interface and return to main menu
                    correct4 = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }while (correct4 == false);
        return;
    }

/************************************************************************************************************************************************************************/

    public static int getId(int y, Scanner input){
        boolean x = false;
        int id = 0;
        do{
            try{
                //print message asking for specific Id
                if(y == 1) System.out.print("Enter Customer Id: ");
                else if (y == 2) System.out.print("Enter Policy Id: ");
                else if (y == 3) System.out.print("Enter Item Id: ");
                else if (y == 4) System.out.print("Enter Person Id: ");
                else if (y == 5) System.out.print("Enter Agent Id: ");
                else if (y == 6) System.out.print("Enter Adjuster Id: ");
                else if (y == 7) System.out.print("Enter Claim Id: ");
                else if (y == 8) System.out.print("Enter Outsource Id: ");
                id = input.nextInt();
                input.nextLine();
                //make sure input is allowable length
                if(id > 999999999){
                    System.out.println("Enter a valid input. Please try again.");
                }
                else x = true;
            }
            //make sure Id is in proper format
            catch(InputMismatchException se){
                System.out.println("Enter a valid input. Please try again.");
                input.next();
            }
            
        }while (x == false);
        //return the Id
        return id;
    }

/************************************************************************************************************************************************************************/

    public static String getString(int y, Scanner input){
    boolean x = false;
    String s = "";
    do{
        //print message asking for specific string
        if(y == 1) System.out.print("Enter First Name: ");
        else if (y == 2) System.out.print("Enter Last Name: ");
        else if (y == 3) System.out.print("Enter Street Name: ");
        else if (y == 4) System.out.print("Enter City: ");
        else if (y == 5) System.out.print("Enter the name of the outsource company: ");
        else if (y == 6) System.out.print("Enter the type of the outsource company: ");
        else if (y == 7) System.out.print("Enter brief description of the claim (less than 100 characters): ");
        else if (y == 8) System.out.print("Enter the model of the car to be insured: ");
        else if (y == 9) System.out.println("Enter the name of the boat to be insured: ");
        s = input.nextLine();
        //make sure string isnt empty and contains only letters and/or spaces
        if(s.isEmpty() || !s.matches("[a-zA-Z\\s]+")){
            System.out.println("Enter a valid input. Please try again.");
        }
        //make sure string is the proper length
        else if(y == 7){
            if(s.length() > 100) System.out.println("Enter a valid input. Please try again.");
            else x = true;
        }
        else if(s.length() > 15){
            System.out.println("Enter a valid input. Please try again.");
        }
        else x = true;
    }while (x == false);
    //return the string
    return s;
}    

/************************************************************************************************************************************************************************/
  
    public static void payMethod(int pay_id, PreparedStatement pStmt, Scanner input, Connection conn){
        try{
            //prompt customer to enter the type of payment method they want to use
            //take inputs and make sure they are in the proper format
            //store payment type in the correct payment generalization
            System.out.println("Would you like to pay using credit, debit, or checking?");
            boolean c = false;
            do{
                System.out.print("Enter CR for credit, D for debit, or CH for checking: ");
                String response = input.nextLine();  
                if(response.equals("CR")){
                    boolean x = false;
                    String num = "";
                    double card = 0;
                    do{
                        try{
                            System.out.print("Enter credit card number (16 digits): ");
                            num = input.nextLine();
                            if(num.length() != 16){
                                System.out.println("Enter a valid input. Please try again.");
                            }
                            else{
                                card = Double.parseDouble(num);  
                                x = true;
                            }
                        }
                        catch(NumberFormatException se){
                            System.out.println("Enter a valid input. Please try again.");
                        }  
                    }while (x == false);
                    x = false;
                    int cvc = 0;
                    do{
                        try{
                            System.out.print("Enter cvc number (3 digits): ");
                            num = input.nextLine();  
                            if(num.length() != 3){
                                System.out.println("Enter a valid input. Please try again.");
                            }
                            else{
                                cvc = Integer.parseInt(num);  
                                x = true;
                            }
                        }
                        catch(NumberFormatException se){
                            System.out.println("Enter a valid input. Please try again.");
                        }  
                    }while (x == false);
                    pStmt = conn.prepareStatement("insert into credit (pay_id, card_num, cvc) values (?, ?, ?)");
                    pStmt.setInt(1, pay_id);
                    pStmt.setDouble(2, card);
                    pStmt.setInt(3, cvc);
                    pStmt.executeUpdate();
                    c = true;
                }
                else if(response.equals("D")){
                    boolean x = false;
                    String num = "";
                    double card = 0;
                    do{
                        try{
                            System.out.print("Enter debit card number (16 digits): ");
                            num = input.nextLine();
                            if(num.length() != 16){
                                System.out.println("Enter a valid input. Please try again.");
                            }
                            else{
                                card = Double.parseDouble(num);  
                                x = true;
                            }
                        }
                        catch(NumberFormatException se){
                            System.out.println("Enter a valid input. Please try again.");
                        }  
                    }while (x == false);
                    x = false;
                    int cvc = 0;
                    do{
                        try{
                            System.out.print("Enter cvc number (3 digits): ");
                            num = input.nextLine();  
                            if(num.length() != 3){
                                System.out.println("Enter a valid input. Please try again.");
                            }
                            else{
                                cvc = Integer.parseInt(num);  
                                x = true;
                            }
                        }
                        catch(NumberFormatException se){
                            System.out.println("Enter a valid input. Please try again.");
                        }  
                    }while (x == false);
                    pStmt = conn.prepareStatement("insert into debit (pay_id, card_num, cvc) values (?, ?, ?)");
                    pStmt.setInt(1, pay_id);
                    pStmt.setDouble(2, card);
                    pStmt.setInt(3, cvc);
                    pStmt.executeUpdate();
                    c = true;
                }
                else if(response.equals("CH")){
                    boolean x = false;
                    String num = "";
                    double account = 0;
                    do{
                        try{
                            System.out.print("Enter checking account number (16 digits): ");
                            num = input.nextLine();
                            if(num.length() != 16){
                                System.out.println("Enter a valid input. Please try again.");
                            }
                            else{
                                account = Double.parseDouble(num);  
                                x = true;
                            }
                        }
                        catch(NumberFormatException se){
                            System.out.println("Enter a valid input. Please try again.");
                        }  
                    }while (x == false);
                    pStmt = conn.prepareStatement("insert into checking (pay_id, account_num) values (?, ?)");
                    pStmt.setInt(1, pay_id);
                    pStmt.setDouble(2, account);
                    pStmt.executeUpdate();
                    c = true;
                }
                else{
                    System.out.println("Enter a valid response. Please try again.");
                }
            }while(c == false);         
        }
        catch(Exception se){
            System.out.println(se);
        }
    }
}
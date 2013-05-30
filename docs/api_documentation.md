# Giving-Socially RESTful JSON API	
---
	
## Authentication		
To work with the API you have to have an Authentication Token, for now you get one as soon as you create an account.	
		
## Common Headers	
The following headers should be sent with all requests to the API:	
`Accept: application/json
Content-Type: application/json; charset=utf-8
Accept-Language: en`	
			
__Base URL:__ __Local:__ http://localhost:3000 OR __Production:__ http://furious-light-8716.herokuapp.com/
	
### Working with Users	
*	Creating a New User:
	
	__URI:__ __/api/users__		
	__Method:__ POST	
	__Request Body Data:__	All details to create the User account.		 
	<pre>
		{
	    "user": {
                 "username" : "barfoo",
       			 "email" :"bar@foo.com",
			     "password" : "foobar",
			     "password_confirmation" : "foobar",
				 "first_name" : "Hello",
				 "last_name"  : "Kitty", 
				 "avatar" : "Base 64 encoded image as String"
        		}
		}
	</pre>		
	__Response Status:__ 201 OK			
	__Response Body Data:__			 
		<pre>
			{
			    "user": {
			        "authentication_token": "dnfvnLfqs3MCbyqsc1Np", 
			        "email": "bar@foo.com",
			 		"first_name" : "Hello",
					"last_name"  : "Kitty",
			        "username": "barfoo"
			    }
			}
		</pre>		
		
*	Signing a User in/Logging in: Use this URI to sign in and retrieve your Authentication Token!						
			
	__URI:__ __/api/users/sign_in__				
	__Request Body Data:__	User Credentials			 
	__*Regular sign in:*__
	<pre>
		{
		    "user": {
		        "email": "bar@foo.com", 
		        "password": "barfoo"
		    }
		}
	</pre>
	__*Facebook sign in:*__ Collect all the data from signing in to Facebook and then pass it on..
	<pre>
		{
		    "user": {
		        "email": "bar@foo.com", 
		        "facebook_token" : "AAAFJt1Gcrl8BACasdfgzxGQO0sWYQ2sEo2wv00ZC1oZCiQP2KrD5sVTz7Fh7zFNvKtNHncvouvOGl4ZChasdfXteYNZCQjIpC4FFStX4DZCs8wZDZD",
				                                "first_name" : "stink",
				                                "last_name" : "bomb"
		    }
		}
	</pre>						
	__Method:__ POST				
	__Response Status:__ 200 OK			
	__Response Body Data:__	 
	<pre>
		{
		    "user": {
		        "authentication_token": "dnfvnLfqs3MCbyqsc1Np", 
		        "email": "bar@foo.com",
				"first_name" : "Hello",
				"last_name"  : "Kitty", 
		        "username": "barfoo"
		    }
		}
	</pre>

*   Sending a friend request to another User: 	
		
    __URI:__ __/api/friends/request/:friend\_username?auth_token=JZoi84yyPLUsnqpoDaQL__		
	__Parameters:__	*:friend_username* indicates the username for the user that you want to send a friend request to.			
	__Method:__ POST		
	__Response Status:__ 200 OK		
	
*	Showing friend requests that need to be confirmed: This will list all the Users that are pending confirmation as friends. 
		
	__URI:__ __/api/friends/pending?auth_token=JZoi84yyPLUsnqpoDaQL__		
	__Method:__ GET		
	__Response Status:__ 200 OK		
	__Response Body Data:__		
	<pre>
		{
		    "users": [
		        {
		            "user": {
		                "email": "person-66@example.com", 
		                "username": "person-66"
		            }
		        }, 
		        {
		            "user": {
		                "email": "person-67@example.com", 
		                "username": "person-67"
		            }
		        }
		    ]
		}
	</pre>	
		
*	Confirming a friend request:	
	
	__URI:__ __/api/friends/confirm/:potential\_friend\_username?auth_token=JZoi84yyPLUsnqpoDaQL__		
	__Parameters:__	*:potential_friend_username* indicates the username for the user that you want to confirm as a friend.			
	__Method:__ POST		
	__Response Status:__ 200 OK
		
*	Showing a User's friends:	
		
	__URI:__ __/api/friends?auth_token=JZoi84yyPLUsnqpoDaQL__						
	__Method:__	GET				
	__Response Status:__ 200 OK			
	__Response Body Data:__		
	<pre>
		{
		    "users": [
		        {
		            "user": {
		                "email": "person-66@example.com", 
		                "username": "person-66"
		            }
		        }, 
		        {
		            "user": {
		                "email": "person-67@example.com", 
		                "username": "person-67"
		            }
		        }
		    ]
		}
	</pre>		
			
*	Finding friends for a User:	Use to find possible friends of the User who are also using the app. 
__Hint:__ Use email addresses from users iPhone contacts of Facebook friends list to search against the app. 
		
	__URI:__ __/api/friends/find__		
	__Method:__ POST	
	__Request Body Data:__ A list of email addresses of people who you think might have accounts with the app.			 
	<pre>
		{
	    "email_addresses": [
                 "person-71@example.com",
       			 "person-72@example.com",
			     "potential_friend3@email.com",
			    ]
		}
	</pre>		
	__Response Status:__ 200 OK		
	__Response Body Data:__	The list of users returned are users that are registered with the app.						
	<pre>
		{
		    "users": [
		        {
		            "user": {
		                "email": "person-71@example.com", 
		                "username": "person-71"
		            }
		        }, 
		        {
		            "user": {
		                "email": "person-72@example.com", 
		                "username": "person-72"
		            }
		        }
		    ]
		}
			
	</pre>	
	
*	Inviting a User to use the app: Currently since we do not have an smtp server setup Mail is not fully configured although it is fully coded and tested with the mail queue. Using the below request and invitation email will be sent to the specified user with a link to the app.			
	
	__URI:__ __/api/users/invite?auth_token=JZoi84yyPLUsnqpoDaQL__		
	__Request Body Data_:__		
	<pre>
		{
            "user": {
                "email": "personToInvite@example.com",		 
                "first_name": "PersonToInviteFirstName",		
				"last_name": "PersonToInviteLastName"
            }
        }
	</pre>			
	__Method:__ POST		
	__Response Status:__ 200 OK
		


### Working with Gift Lists
*   Create a New Gift List for a User:	
		
    __URI:__ __/api/gift\_lists?auth_token=JZoi84yyPLUsnqpoDaQL__		
	__Method:__ POST		
	__Request Body Data:__ Details for the new Gift List you would like to create.				
		<pre>
			{
			    "gift_list": {
			        "name" => "Test Gift List",
					"is_private" => "false",
					"is_editable_by_friends" => "true",
					"due_date" => "2012-03-09 04:29:25 +0000"
			    }
			}
		</pre>			
	__Response Status:__ 201 OK

*   Showing all the Gift Lists for a User:	
		
    __URI:__ __/api/gift\_lists?auth_token=JZoi84yyPLUsnqpoDaQL__						
	__Method:__ GET				
	__Response Body Data:__ A list of all the gift lists that a user has access to.				
		<pre>
			{
			    "gift_lists": [
			        {
			            "gift_list": {
			                "all\_gifts_purchased": false, 
			                "due_date": "2015-10-10T00:00:00Z", 
			                "id": 1, 
			                "is\_editable\_by_friends": false, 
			                "is_private": false, 
			                "is_starred": false, 
			                "name": "Da Bomb!", 
			                "purpose": "Foobars Birthday"
			            }
			        }, 
			        {
			            "gift_list": {
			                "all\_gifts_purchased": false, 
			                "due_date": "2015-10-10T00:00:00Z", 
			                "id": 2, 
			                "is\_editable\_by_friends": false, 
			                "is_private": false, 
			                "is_starred": false, 
			                "name": "BooYAList-15", 
			                "purpose": "Foobars Birthday"
			            }
			        }
			    ]
			}	
		</pre>				
	__Response Status:__ 200 OK		
	
*	Adding/Sharing a GiftList with a Friend:	
	
 	__URI:__ __/api/gift_lists/:id/share?auth\_token=JZoi84yyPLUsnqpoDaQL__						
	__Parameters:__ *:id* indicates the id of the Gift List you want to share.		
	__Method:__ POST		
	__Request Body Data:__	Should contain a list of Friends with respective privileges to the Gift List.				
		<pre>
		{
		    "friend_privileges": [
		        {
		            "is_contributor": false, 
		            "is_restricted": true, 
		            "username": "person-1"
		        }, 
		        {
		            "is_contributor": false, 
		            "is_restricted": true, 
		            "username": "person-2"
		        }
		    ]
		}
		</pre>					
	__Response Status:__ 201 OK		
		
### Working with Gifts	
	
*	Adding a Gift to a GiftList: Will allow a User to add a Gift to a GiftList that he/she Created or has Editing privileges over.	
		
	__URI:__ __/api/gift_lists/:id/gifts?auth\_token=JZoi84yyPLUsnqpoDaQL__					
	__Parameters__: *:id* indicates the id of the gift_list you want to add gifts to.		
	__Method:__ POST		
	__Request Body Data:__ Should contain details of a gift that you want to add to the Gift List.				
	<pre>
		{
			"gift" : {
			              "name" : "Mac Book Pro",
			              "amazon\_affiliate\_link" : "http://www.amazon.com/Apple-MacBook-MD318LL-15-4-Inch-VERSION/dp/B005CWJ1DI/ref=sr_1_3?s=electronics&ie=UTF8&qid=1326746909&sr=1-3",
			              "is_purchased" : false,
			              "approximate_price" : 12.50,
			              "link\_to\_example" : "http://www.amazon.com/Apple-MacBook-MD318LL-15-4-Inch-VERSION/dp/B005CWJ1DI/ref=sr_1_3?s=electronics&ie=UTF8&qid=1326746909&sr=1-3",
			         	  "gift_image" : "Base 64 encoded image as a String"
					}
        }
	</pre>		
	__Response Status:__ 201 OK	
	
*	Showing all the Gifts in a GiftList: Will allow the User to show all the Gifts in a GiftList he/she has access to.	
		
	__URI:__ __/api/gift_lists/:id/gifts?auth\_token=JZoi84yyPLUsnqpoDaQL__						
	__Parameters:__ *:id* indicates the of of the Gift List whose gifts you would like to show.		
	__Method:__ GET		
	__Response Body Data:__						
	<pre>
		{
		    "gifts": [
		        {
		            "gift": {
		                "amazon_affiliate_link": "http://www.amazon.com/Apple-MacBook-MD318LL-15-4-Inch-VERSION/dp/B005CWJ1DI/ref=sr_1_3?s=electronics&ie=UTF8&qid=1326746909&sr=1-3", 
		                "approximate_price": "12.5", 
		                "id": 2, 
		                "is_purchased": false, 
		                "link_to_example": "http://www.amazon.com/Apple-MacBook-MD318LL-15-4-Inch-VERSION/dp/B005CWJ1DI/ref=sr_1_3?s=electronics&ie=UTF8&qid=1326746909&sr=1-3", 
		                "name": "Super-Gift-22"
		            }
		        }, 
		        {
		            "gift": {
		                "amazon_affiliate_link": "http://www.amazon.com/Apple-MacBook-MD318LL-15-4-Inch-VERSION/dp/B005CWJ1DI/ref=sr_1_3?s=electronics&ie=UTF8&qid=1326746909&sr=1-3", 
		                "approximate_price": "12.5", 
		                "id": 3, 
		                "is_purchased": false, 
		                "link_to_example": "http://www.amazon.com/Apple-MacBook-MD318LL-15-4-Inch-VERSION/dp/B005CWJ1DI/ref=sr_1_3?s=electronics&ie=UTF8&qid=1326746909&sr=1-3", 
		                "name": "Super-Gift-23"
		            }
		        }, 
		        {
		            "gift": {
		                "amazon_affiliate_link": "http://www.amazon.com/Apple-MacBook-MD318LL-15-4-Inch-VERSION/dp/B005CWJ1DI/ref=sr_1_3?s=electronics&ie=UTF8&qid=1326746909&sr=1-3", 
		                "approximate_price": "12.5", 
		                "id": 4, 
		                "is_purchased": false, 
		                "link_to_example": "http://www.amazon.com/Apple-MacBook-MD318LL-15-4-Inch-VERSION/dp/B005CWJ1DI/ref=sr_1_3?s=electronics&ie=UTF8&qid=1326746909&sr=1-3", 
		                "name": "Super-Gift-24"
		            }
		        }
		    ]
		}
	</pre>								
	__Response Status:__ 200 OK		
	
*	Changing the Purchased State of a Gift: Will allow a User to change the Purchased State of a Gift in a GiftList.	
		
	__URI:__ __/api/gifts/:id/purchased/:is_purchased?auth\_token=JZoi84yyPLUsnqpoDaQL__				
	__Parameters:__ *:id* indicates the id of the Gift you want to manipulate. *:id_purchased* can be __true__ or __false__		
	__Method:__ POST					
	__Response Status:__ 200 OK	
	
### Working with Activities:	
	
*	Showing the Activities for a User:		
	
	__URI:__ __/api/activities?auth\_token=JZoi84yyPLUsnqpoDaQL__						
	__Method:__ GET		
	__Response Body Data:__	
	<pre>
		{
		    "activities": [
		        {
		            "activity": {
		                "friendly_descriptor": "person-1 added Mac Book Pro to Da Bomb!", 
		                "gift": {
		                    "amazon_affiliate_link": "http://www.amazon.com/Apple-MacBook-MD318LL-15-4-Inch-VERSION/dp/B005CWJ1DI/ref=sr_1_3?s=electronics&ie=UTF8&qid=1326746909&sr=1-3", 
		                    "approximate_price": "12.5", 
		                    "gift_list_id": 1, 
		                    "id": 1, 
		                    "is_purchased": false, 
		                    "link_to_example": "http://www.amazon.com/Apple-MacBook-MD318LL-15-4-Inch-VERSION/dp/B005CWJ1DI/ref=sr_1_3?s=electronics&ie=UTF8&qid=1326746909&sr=1-3", 
		                    "name": "Mac Book Pro"
		                }, 
		                "user": {
		                    "email": "person-1@example.com", 
		                    "username": "person-1"
		                }
		            }
		        }, 
		        {
		            "activity": {
		                "friendly_descriptor": "Super-Gift-1 has become available for purchase on Da Bomb!.", 
		                "gift": {
		                    "amazon_affiliate_link": "http://www.amazon.com/Apple-MacBook-MD318LL-15-4-Inch-VERSION/dp/B005CWJ1DI/ref=sr_1_3?s=electronics&ie=UTF8&qid=1326746909&sr=1-3", 
		                    "approximate_price": "12.5", 
		                    "gift_list_id": 1, 
		                    "id": 2, 
		                    "is_purchased": false, 
		                    "link_to_example": "http://www.amazon.com/Apple-MacBook-MD318LL-15-4-Inch-VERSION/dp/B005CWJ1DI/ref=sr_1_3?s=electronics&ie=UTF8&qid=1326746909&sr=1-3", 
		                    "name": "Super-Gift-1"
		                }, 
		                "user": {
		                    "email": "person-2@example.com", 
		                    "username": "person-2"
		                }
		            }
		        },
		        {
		            "activity": {
		                "friendly_descriptor": "person-1 shared Da Bomb!.", 
		                "gift_list": {
		                    "all_gifts_purchased": false, 
		                    "due_date": "2015-10-10T00:00:00Z", 
		                    "id": 1, 
		                    "is_editable_by_friends": false, 
		                    "is_private": false, 
		                    "is_starred": false, 
		                    "name": "Da Bomb!", 
		                    "purpose": "Foobars Birthday"
		                }, 
		                "user": {
		                    "email": "person-1@example.com", 
		                    "username": "person-1"
		                }
		            }
		        }
		    ]
		}
		
	
	</pre>

### Working with Gift Comments:
	
*	Creating a comment for a Gift: 
	
	__URI:__ __/gift_comments?auth\_token=JZoi84yyPLUsnqpoDaQL__					
	__Method:__ POST		
	__Request Body Data:__ Should contain details of a Comment to be created for a Gift				
	<pre>
		{
			"gift_comment" : {
			              "comment" : "What a sexy Gift!",
			              "gift_id" : "3",
			              "image" : "Base 64 encoded image as a String"
					}
	    }
	</pre>		
	__Response Status:__ 201 OK
	__Response Body Data:__		
	<pre>
		{
			"gift_comment" : {
			              "comment" : "What a sexy Gift!",
			        }
	    }
	</pre>
	
*	Showing all the comments for a Gift:
		
	__URI:__ __/gift/:id/gift_comments?auth\_token=JZoi84yyPLUsnqpoDaQL__					
	__Method:__ GET
	__Response Body Data:__	
	<pre>
		{
		    "gift_comments": [
		        {
		            "gift_comment": {
		                "comment": "Comment-4", 
		                "id": 5, 
		                "user_id": 1,
						"image" : "Base 64 encoded image as a String"
		            }
		        }, 
		        {
		            "gift_comment": {
		                "comment": "Comment-3", 
		                "id": 4, 
		                "user_id": 1,
						"image" : "Base 64 encoded image as a String"
		            }
		        }
		    ]
		}
	
	</pre>



###	Current Test Users at http://furious-light-8716.herokuapp.com/:
		
Password for all Users: `kitten_little`	



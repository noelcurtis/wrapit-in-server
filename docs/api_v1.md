# WrapIt.in RESTful JSON API  

## Common Headers	
The following headers should be sent with all requests to the API:	
`Accept: application/json
Content-Type: application/json; charset=utf-8
Accept-Language: en`	
			
__Base URL:__ __Local:__ http://localhost:9000 OR __Production:__ http://wrapitin.herokuapp.com/

### Creating A User

__URI:__ __/apiv1/users__		
__Method:__ POST	
__Request Body Data:__	All details to create the User account.		 
<pre>
	{
    "username" : "barfoo",
    "email" :"bar@foo.com",
    "password" : "foobar",
    "passwordConfirmation" : "foobar"
	}
</pre>		
__Response Status:__ 201 OK			
__Response Body Data:__			 
<pre>
	{
    "authToken": "dnfvnLfqs3MCbyqsc1Np", 
    "email": "bar@foo.com",
	}
</pre>
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
    "email" :"bar@foo.com",
    "password" : "foobar"
	}
</pre>		
__Response Status:__ 201 OK			
__Response Body Data:__			 
<pre>
	{
    "email":"foobar@gmail.com",
    "lastSignIn":1374721831445, // date should be in this form only
    "token":"a"
  }
</pre>


### Authenticating A User

__URI:__ __/apiv1/authenticate		
__Method:__ POST	
__Request Body Data:__	All details to auth the User account.		 
<pre>
	{
    "email" :"bar@foo.com",
    "password" : "foobar"
	}
</pre>		
__Response Status:__ 200 OK			
__Response Body Data:__			 
<pre>
	{
    "email":"foobar@gmail.com",
    "lastSignIn":1374721831445, // date should be in this form only
    "token":"a"
  }
</pre>


### Getting Lists

http://localhost:9000/apiv1/lists
GET
Auth-Token: sometoken

  [
      {
          "role": 1,
          "giftList": {
              "id": 1,
              "name": "A List by Foobar",
              "dueDate": 1378676412588,
              "itemCount": 4
          }
      },
      {
          "role": 1,
          "giftList": {
              "id": 2,
              "name": "A List For Ann",
              "dueDate": 1379022012588,
              "itemCount": 4
          }
      }
  ]      

### Creating Lists

http://localhost:9000/apiv1/lists
POST
Auth-Token: sometoken

{
  "name": "A List by Bob",
  "dueDate": 1378351031636
}

response

{
    "role": 1,
    "giftList": {
        "id": 8,
        "name": "A List by Bob",
        "dueDate": 1378351031636,
        "itemCount": 0
    }
}


### Gift List Photos

http://localhost:9000/apiv1/lists/photos
GET

{
    "1": [
        "https://s3.amazonaws.com/wi-dev/items/5c8b9424e5c5787296856b141599b9bb09f2365310b565e123ebd15bfb3ea310.jpeg",
        "https://s3.amazonaws.com/wi-dev/items/e72b9ead212e1ea6f57da0603be32662b76da4bcad609277ec556978e76dac92.jpeg",
        "https://s3.amazonaws.com/wi-dev/items/cecc3f242584eab47def94b528f1106ac335351fc275dc4252e3d762f8e46c1e.jpeg"
    ],
    "2": [
        "https://s3.amazonaws.com/wi-dev/items/5c8b9424e5c5787296856b141599b9bb09f2365310b565e123ebd15bfb3ea310.jpeg",
        "https://s3.amazonaws.com/wi-dev/items/e72b9ead212e1ea6f57da0603be32662b76da4bcad609277ec556978e76dac92.jpeg",
        "https://s3.amazonaws.com/wi-dev/items/cecc3f242584eab47def94b528f1106ac335351fc275dc4252e3d762f8e46c1e.jpeg"
    ]
}

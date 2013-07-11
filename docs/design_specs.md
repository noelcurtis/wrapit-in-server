## New Gift Screen

Name
Link
Get Images YES/NO
Number

Done!

If Get Images selected
- Scrape images
- Show images option
- allow to select 1 image
- add selected image to item



## S3 Photos

Item should have getPhoto:Option[Photo]

* Looks at an Item
* User selects a photo
* Get the URL
* Resolve the Image and push it to AWS
* Create a PhotoRelation and add Item/Photo to it


## Facebook Auth

Following steps should be taken to check whether user is authenticated

* Check the session for email
* If user is a facebook user check the validity of their Facebook token
* If their facebook token is valid, add email to the session and contiue as logged in
* else clear the session and redirect to the login page


##  Comments

* Users can comment on an Item
* User -> CommentRelation ->  Comment
                          ->  Item
* To create a comment: First create the comment, then create the comment relation. All via ajax. 


## Photo Changes

* For each array of photos
* Start a new changing function
*                          









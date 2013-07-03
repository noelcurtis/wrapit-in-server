$(function(){

    // listen for Return on the Comments field
    $("#item-detail-comment").keyup(function(event){
        if (event.which == 13) {
            event.preventDefault();
            itemDetailSubmitComment(itdGiftListId, itdItemID, $(this).val());
        }
    })

});

// Use to submit a comment via ajax
function itemDetailSubmitComment(giftListId, itemId, comment) {
    if (typeof comment == "undefined" || comment == "") return;
    // submit the comment via post
    var data = {comment: comment}
    console.log(data)
    $.post("/list/"+giftListId+"/items/"+itemId+"/comment", data).done(function(data) {
        console.log(data);
    }).fail(function(data) {
        console.log(data);
    });
}
function toggleCheck(e)
{
    var label = $(e).find("label.checkbox").first();
    if ($(label).hasClass("checked"))
    {
        $(label).removeClass("checked");
    }
    else
    {
        $(label).addClass("checked");
    }
}


// Returns a random number between min and max
function getRandom(min, max)
{
    return Math.random() * (max - min) + min;
}
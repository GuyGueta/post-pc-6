I pledge the highest level of ethical principles in support of academic excellence.
I ensure that all of my work reflects my own abilities and not those of someone else."

Q answer :
in order to change the notification text, we should store the notification id in the intent
(along with the number and message content) -
after  we will call notify() with the same id it will change the existing notification
instead of creating a new one.
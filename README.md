# ST-NuHeat-Signature-Thermostat

This device handler is for controlling the [Nuheat Signature Floor Thermostat](http://www.nuheat.com/products/thermostats/signature). They are available at [Amazon](https://www.amazon.com/SIGNATURE-Touchscreen-Programmable-Dual-Voltage-Thermostat/dp/B00KHA6MEG) for about $190 (July '17).

A couple items you should know...

1. This users the Nuheat API so you must have an internet connection for it to work.
2. Nuheat has not officially published an API so it can change without warning.
3. I did not use the Async beta to build this, but I have since updated a few of my other DH so I'll likely update this.
4. I designed the DH to how I thought I would use it. Let me know if you want changes. I can't make any promises.
5. To use this DH you will need your Nuheat username and password along with your thermostat(s) id. You can find the thermostat ID at [online](http://mynuheat.com). Go into the thermostat and it is the "Thermostat ID".

![](http://i.imgur.com/6X8k5ZC.png)

![](http://i.imgur.com/ge0bhIc.png)

**Name** - Just pick something, required

**Thermostate Serial Number** - from the website, required

**Default Hold Time** - If you manually change the temp, it will be set to hold that time for this duration, required

![](http://i.imgur.com/jjHOCym.png)

**Default On Temperature** - If the on() method is called it will use this temp as the heating set point.

**Power Usage** - This device will report power usage. However, the API was not reliable when calculating it. So if you want to use it, you have to enter the value you think it is using based on the length of your cable. Sorry, this is the best I could do, most of the time the API reports 0W.
Auto Refresh - Interval at which the DH will poll Nuheat for changes. You cannot change this. I wouldn't recommend changing it in the code to make it faster.

**Username** - Your Nuheat username

**Password** - Your Nuheat password

**Show Password in Log** - For debugging, leave it off unless you want to see the password in the logs.

**Log Level** - Amount of log detail.

<table><tr><td> <em>Assignment: </em> IT114 Chatroom Milestone 2</td></tr>
<tr><td> <em>Student: </em> Matthew Ferrari (mjf8)</td></tr>
<tr><td> <em>Generated: </em> 11/10/2023 11:56:28 PM</td></tr>
<tr><td> <em>Grading Link: </em> <a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-001-F23/it114-chatroom-milestone-2/grade/mjf8" target="_blank">Grading</a></td></tr></table>
<table><tr><td> <em>Instructions: </em> <p>Implement the features from Milestone2 from the proposal document:&nbsp; <a href="https://docs.google.com/document/d/1ONmvEvel97GTFPGfVwwQC96xSsobbSbk56145XizQG4/view">https://docs.google.com/document/d/1ONmvEvel97GTFPGfVwwQC96xSsobbSbk56145XizQG4/view</a></p>
</td></tr></table>
<table><tr><td> <em>Deliverable 1: </em> Payload </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Payload Screenshots</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fmjf8%2F2023-11-07T15.24.02Screenshot%202023-11-07%20at%2010.23.44%20AM.png.webp?alt=media&token=2aa78fb9-e3df-44d5-af11-04b23c35156c"/></td></tr>
<tr><td> <em>Caption:</em> <p>In this file, we see the Payload class. All of the methods implemented<br>in here are to handle server payload, or &quot;traffic&quot;. (mjf8)<br></p>
</td></tr>
</table></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 2: </em> Server-side commands </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Show the code for the mentioned commands</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fmjf8%2F2023-11-07T15.25.47Screenshot%202023-11-07%20at%2010.25.40%20AM.png.webp?alt=media&token=cdbd507f-5c33-47df-86be-aee3f6138091"/></td></tr>
<tr><td> <em>Caption:</em> <p>In this image, we see the method implementation for /roll, which shows both<br>formats. There are roll methods in Room.java on my repository.<br></p>
</td></tr>
<tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fmjf8%2F2023-11-07T15.27.07Screenshot%202023-11-07%20at%2010.26.20%20AM.png.webp?alt=media&token=0e66c390-5488-4b71-b2d2-dbf2909d030b"/></td></tr>
<tr><td> <em>Caption:</em> <p>In this image, we see the method implementation for /flip. There are methods<br>in Room.java on my repository.<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Explain the logic/implementation of each commands</td></tr>
<tr><td> <em>Response:</em> <p>The &quot;/roll&quot; command works in two ways. First, it checks whether the first<br>element of the command starts with COMMAND_TRIGGER, which is globally set to &quot;/&quot;.<br>Then, it checks the second element of the command (comm2[1]) and checks if<br>it&#39;s equal to the character &quot;d&quot;.&nbsp;<br><br>If the command&#39;s second element is not equal<br>to &quot;d&quot;, it will assume that the user would like to process the<br>command as /roll #, where the only variable is the number of sides<br>on the die. On the other hand, if the second element contains &quot;d&quot;,<br>it will assume that the user would like to process the command as<br>/roll #d#, where the variables are number of dice and number of sides,<br>respectively.&nbsp;<br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 3: </em> Text Display </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Show the code for the various style handling via markdown or special characters</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fmjf8%2F2023-11-07T15.29.38Screenshot%202023-11-07%20at%2010.29.28%20AM.png.webp?alt=media&token=43686209-3254-4320-9e4c-0a7dc28d8b2e"/></td></tr>
<tr><td> <em>Caption:</em> <p>This code contains a method for handling special text characters and converting them<br>into HTML characters for use later in Milestone 3. <br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Show source message and the result output in the terminal (note, you won't actually see the styles until Milestone3)</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fmjf8%2F2023-11-11T04.49.29Screenshot%202023-11-10%20at%2011.49.06%20PM.png.webp?alt=media&token=0a0653f2-7a8f-4f7b-aa5e-e25de6e33bc9"/></td></tr>
<tr><td> <em>Caption:</em> <p>In this image, we can see the HTML tags that are generated for<br>bold, italic, and underline separately, and then all three together. In the second<br>message, we can see how colored text works and how colored text works<br>with bold, italic, and underline.<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 3: </em> Explain how you got each style applied</td></tr>
<tr><td> <em>Response:</em> <p>To get each HTML tag set for styling, I used the replaceAll function.<br>For example, if a user wraps their text in the format &quot;<strong>bold</strong>&quot; this<br>would replace the first &quot;<strong>&quot; block with &quot;&lt;b&gt;&quot; and the second &quot;</strong>&quot; block<br>with &lt;/b&gt;. This works for italics (&lt;i&gt; &lt;/i&gt; from &quot;<em>text</em>&quot;) and underline as<br>well (&lt;u&gt; &lt;/u&gt; from &quot;<em>text</em>&quot;).&nbsp;<div><br></div><div>Similarly, color is handled by changing plaintext tags, in<br>this case #r, #g, #b, to &lt;font&gt; tags in HTML.</div><br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 4: </em> Misc </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Include the pull request for Milestone2 to main</td></tr>
<tr><td> <a rel="noreferrer noopener" target="_blank" href="https://github.com/mattferrari9/MJF8-IT114-01/commit/ffec0471b244b2129bf158e8a97b0e8cbf90ec81">https://github.com/mattferrari9/MJF8-IT114-01/commit/ffec0471b244b2129bf158e8a97b0e8cbf90ec81</a> </td></tr>
</table></td></tr>
<table><tr><td><em>Grading Link: </em><a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-001-F23/it114-chatroom-milestone-2/grade/mjf8" target="_blank">Grading</a></td></tr></table>
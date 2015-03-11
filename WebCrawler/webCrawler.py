'''
Created on Sep 21, 2014

@author: NITISH
'''

import urllib2
import time
import sys
import datetime

from bs4 import BeautifulSoup

class Node(object):
    def __init__(self):
        self.next=None
        self.link=None
        self.parent=None
        self.level=None
        self.nextStep=None
        self.position=0
        
class LinkedList(object):
    def __init__(self):
        self.head=None
        
    def add(self,link,parent,level,nextStep):
        current=self.head
        temp = Node()
        temp.link=link
        temp.parent=parent
        temp.level=level
        temp.nextStep=nextStep
        temp.position = temp.position + 1
        "if head is empty..fill it first"
        if(self.head==None):
            self.head=temp
            return
        "traverse through the entire linked list"
        while(current.next!=None):
            current=current.next
        current.next=temp
        
    
    def printLL(self):
        if(self.head==None):
            print "no link to print"
            return
        current=self.head
        while(current!=None):
            print 'link: ' + current.link
            current=current.next
            
            
            
    def printLinks(self):
        if(self.head==None):
            print "no link to print"
            return
        current=self.head
        while(current!=None):
            print 'link: ' + current.link
            print 'level: ' + str(current.level)
            print '*****************************'
            current=current.next
    
    def search(self,link):
        current = self.head
#         found = False
        while current != None:
            if(current.link == link):
                return True
            else:
                current = current.next

        return False
    
    def getCanonicalLink(self,soup):
        
        array = []
        links = soup.findAll('link', rel='canonical')
        for link in links:
            array.append(link['href'])
        
        return array[0]
        
            
    def countNodes(self):
        if(self.head==None):
            return 0
        current=self.head
        totalcount=0
        count1=0
        count2=0
        count3=0
        while(current!=None):
            totalcount = totalcount + 1
            if(current.level ==1):
                count1 = count1 + 1
            if(current.level ==2):
                count2 = count2 + 1
            if(current.level ==3):
                count3 = count3 + 1
            
            current=current.next
        
        print "links at level 1 :" + str(count1)
        print "links at level 2 :" + str(count2)
        print "links at level 3 :" + str(count3)
            
        print "total links : " + str(totalcount)

        
    def crawler(self,url,keyphrase):
        rootParent=None
        rootLevel = 1

        self.add(url,rootParent,rootLevel,'crawl')
        
        if(self.head==None):
            print "no link to crawl"
            return
        current=self.head
        
        while(current!=None): 
            
            if(current.level==3):
                print "reached depth " + str(current.level) + "and parsed these pages"
                return
            
            print 'current seed ' + current.link
            print 'crawling links on level ' + str(current.level)
#             print '********************************************************************************************************'
#             print 'level: ' + str(current.level)
#             if(current.parent!=None):
#                 print 'parent: ' + current.parent
#             print 'next-step: ' + current.nextStep
#             print '*****************************'
            
            #taking the current link and getting the html page and links
            
            try:
                response = urllib2.urlopen(current.link)
                html = response.read()
                soup = BeautifulSoup(html)
            
                text = soup.get_text()
#                 time.sleep(1) # delays for 1 second
            
                if(keyphrase.lower() in text.lower()):
                    for anchor in soup.find_all('a'):
                        obtainedLink = anchor.get('href')
                        if(obtainedLink!=None and obtainedLink.startswith('/wiki/') and (not ':' in obtainedLink)  and (not '#' in obtainedLink) and (not '/wiki/Main_Page' in obtainedLink)):
                            responseLast = urllib2.urlopen('http://en.wikipedia.org'+obtainedLink)
                            htmlLast = responseLast.read()
                            soupLast = BeautifulSoup(htmlLast)
                            textLast = soupLast.get_text()
                            actual_link = self.getCanonicalLink(soupLast)
                            if(self.search(actual_link) != True):
                                if(len(keyphrase) > 0):
#                                     print 'http://en.wikipedia.org'+obtainedLink + "  level 2"
                                    if(keyphrase.lower() in textLast.lower()):
                                        self.add(actual_link, current.link, current.level + 1, 'crawl')
                                else:
                                    self.add(actual_link, current.link, current.level + 1, 'crawl')
    
            except Exception:
                pass
                        
            current=current.next
            
            


url = 'http://en.wikipedia.org/wiki/Gerard_Salton'

LL1 = LinkedList()
LL2 = LinkedList()


# LL1.crawler('http://en.wikipedia.org/wiki/Gerard_Salton','')
# LL1.countNodes()
# LL1.printLL()
print "############################################################################"

input1 = raw_input(" Do you want to enter keyphrase information retrieval (yes or no) ")



ts = time.time()
st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
print st

if(input1.lower() == 'no'): 
    LL2.crawler('http://en.wikipedia.org/wiki/Gerard_Salton','')
else:
    LL2.crawler('http://en.wikipedia.org/wiki/Gerard_Salton','information retrieval')


sys.stdout = open('crawlerLinks.txt', 'w')
print '******************************************************'
print 'Following are the links : '
LL2.printLL()

print '***************************************************'
LL2.countNodes()


ts = time.time()
st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
print st
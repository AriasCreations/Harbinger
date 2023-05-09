#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdio.h>

int main (int argc, char *argv[])
{
    /*
    Make sure after compiling to run the following commands
    chown root <binary>
    chmod u=rwx,g=rx,+s <binary>
    */
    if(setuid (0)==0){
        //printf("Program execution should succeed\n");
    }else{
        //printf("Program execution will most likely fail\n");
    }

    if(getuid()==0){
        //printf("\nProgram is now executing as root\n*Privilege Escalation success!\n");
    }else{
        //printf("\n** ERROR: Privilege escalation has failed\n");
        return -1;
    }

    system ("/bin/bash /bin/pesc_harbinger_update_runner");

    return 0;
}
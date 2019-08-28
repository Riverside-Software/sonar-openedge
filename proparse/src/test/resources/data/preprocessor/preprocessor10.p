/* { preprocessor10.i            &myParam }
{ preprocessor10.i &abc=1     &myParam }
{ preprocessor10.i &myParam=* &abc     }
{ preprocessor10.i &myParam=* &abc=1   } */
{ preprocessor10.i &abc       &myParam }
{ preprocessor10.i &myParam   &abc     }
{ preprocessor10.i &myParam   &abc=1   }

# jslim generated plot file
set yrange [1:10000000]
set xrange [0.5:1.5]
set logscale y
set data style boxes
set boxwidth 0.1
set style fill solid 0.6
set xtics ()
set ylabel "word forms"
set grid
plot 'test.data' using ($1):($4) title 'accepted'\
,'test.data' using ($1+0.1):($5) title 'rejected'

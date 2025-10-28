Um sistema distribuído de contagem em Java, no qual um programa D (Distribuidor) gera um grande vetor de números inteiros aleatórios do tipo byte entre -100 e 100, 
escolhe aleatoriamente uma posição dele, de onde toma o número, cujo número de ocorrências deve ser descoberto e o envia, juntamente com partes do vetor, 
a diferentes programas R (Receptores), que executam a contagem em paralelo.

A comunicação entre D e R ocorre via TCP/IP, utilizando serialização de objetos e conexões persistentes. Cada servidor R mantém sua conexão aberta até receber 
explicitamente um comunicado de encerramento.

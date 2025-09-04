```bash
#!/bin/bash

# SID megadása
SID="S-1-5-21-1004336348-1177238915-682003330-512"

# SID részekre bontása
IFS='-' read -r -a parts <<< "$SID"
revision=${parts[1]}  # 1
identifier_authority=${parts[2]}  # 5
sub_authorities=("${parts[@]:3}")  # 21, 1004336348, 1177238915, 682003330, 512
sub_auth_count=${#sub_authorities[@]}  # 4

# Bináris SID generálása (hexadecimális formában)
binary=""
# Revision (1 bájt)
binary+=$(printf "%02x" "$revision")
# SubAuthorityCount (1 bájt)
binary+=$(printf "%02x" "$sub_auth_count")
# IdentifierAuthority (6 bájt, 0x000000000005 a 5-nek)
binary+="0000000000$(printf "%02x" "$identifier_authority")"
# SubAuthorities (little-endian 4 bájtos egészek)
for sub_auth in "${sub_authorities[@]}"; do
    # Szám konvertálása little-endian hexadecimális formátumba
    hex=$(printf "%08x" "$sub_auth" | sed 's/\(..\)\(..\)\(..\)\(..\)/\4\3\2\1/')
    binary+="$hex"
done

# ASN.1 Octet String kódolás OpenSSL segítségével
# Írjuk a bináris adatokat egy ideiglenes fájlba
echo -n "$binary" | xxd -r -p > temp.bin
# OpenSSL-lel DER Octet String készítése (0x04 tag + hossz + adatok)
asn1_octet=$(openssl asn1parse -genstr "OCTETSTRING:$binary" -noout -out - | xxd -p -c 256 | tr -d '\n')

# Kimenet
echo "SID: $SID"
echo "ASN1:OCTETSTRING:$asn1_octet"

# Tisztítás
rm -f temp.bin
```
import os
import xml.etree.ElementTree as ET
try:
    from lxml import etree
    HAS_LXML = True
except ImportError:
    HAS_LXML = False

def validate_xml_files(data_dir):
    if not HAS_LXML:
        print("[Error] Se requiere 'lxml' para validar contra XSD. Instálalo con: pip install lxml")
        return

    print(f"=== Iniciando Validación Masiva de XMLs contra XSD ===")    
    total_files = 0
    passed = 0
    failed = []

    for root, _, files in os.walk(data_dir):
        for file in files:
            if file.endswith('.xml'):
                total_files += 1
                xml_path = os.path.join(root, file)
                try:
                    xml_doc = etree.parse(xml_path)
                    root_elem = xml_doc.getroot()
                    
                    # Extraer el atributo xsi:noNamespaceSchemaLocation
                    xsd_attr = root_elem.get('{http://www.w3.org/2001/XMLSchema-instance}noNamespaceSchemaLocation')
                    if xsd_attr:
                        # La ruta del xsd normalmente es algo como "xsd/SkillLearn.xsd" o "../../xsd/skills.xsd"
                        # Ajustar el path relativo desde el XML hasta el XSD
                        # En L2J Mobius, el esquema se resuelve asumiendo 'data/' como root, 
                        # pero resolveremos de forma conservadora.
                        xsd_path = os.path.normpath(os.path.join(data_dir, xsd_attr))
                        
                        if not os.path.exists(xsd_path):
                            # Fallback asumiendo rutas relativas o 'xsd/' dentro de data/
                            xsd_path_alt = os.path.join(data_dir, "xsd", os.path.basename(xsd_attr))
                            if os.path.exists(xsd_path_alt):
                                xsd_path = xsd_path_alt
                            else:
                                failed.append((xml_path, f"XSD no encontrado: {xsd_attr}"))
                                continue
                                
                        with open(xsd_path, 'r', encoding='utf-8') as f:
                            schema_doc = etree.parse(f)
                            schema = etree.XMLSchema(schema_doc)
                            
                        # Validar!
                        if schema.validate(xml_doc):
                            passed += 1
                        else:
                            failed.append((xml_path, str(schema.error_log.last_error)))
                    else:
                        failed.append((xml_path, "No especifica XSD (xsi:noNamespaceSchemaLocation faltante)"))
                except Exception as e:
                    failed.append((xml_path, f"Error de parseo: {str(e)}"))

    print("\n=== Resumen de Validación ===")
    print(f"Total XML escaneados: {total_files}")
    print(f"Aprobados: {passed}")
    print(f"Con Errores o Sin XSD: {len(failed)}")
    
    if failed:
        print("\n--- Detalles de Errores ---")
        for f, error in failed:
            rel_path = os.path.relpath(f, data_dir)
            print(f"[X] {rel_path} -> {error}")

if __name__ == '__main__':
    # Directorio hardcodeado a la ruta del Datapack
    data_directory = "/home/fernando/L2J_Mobius/L2J_Mobius_C4_ScionsOfDestiny/dist/game/data"
    validate_xml_files(data_directory)

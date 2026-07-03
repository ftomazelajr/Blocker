# Blocker

App Android nativo que fecha instantaneamente qualquer app/site de pornografia
usando um AccessibilityService, e mantém um contador de dias limpo.

## Subir pelo Termux

cat > README.md << 'EOF'
# Blocker

App Android nativo que fecha instantaneamente qualquer app/site de pornografia
usando um AccessibilityService, e mantém um contador de dias limpo.

## Subir pelo Termux

pkg install git -y
cd Blocker
git init
git add .
git commit -m "Blocker inicial"
git branch -M main
git remote add origin https://github.com/ftomazelajr/Blocker.git
git push -u origin main

## Baixar o APK

1. Vá em github.com/ftomazelajr/Blocker → aba Actions
2. Abra o último workflow "Build APK" (executa sozinho a cada push)
3. Baixe o artifact blocker-debug-apk e instale no celular

## Depois de instalar

1. Abra o app → cria sua senha (PIN) de proteção
2. "Ativar serviço de bloqueio" → ative o Blocker na tela de Acessibilidade do Android
3. "Ativar proteção contra desinstalação" → confirma o Device Admin
4. Pronto — qualquer site/app da lista em BlockList.kt é fechado na hora, e tentar desativar/desinstalar sem o PIN também é bloqueado

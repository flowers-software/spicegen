
.PHONY: build fmt

build:
	goreleaser release --snapshot --clean
	cp ./dist/spicegen_darwin_arm64*/spicegen_darwin_arm64* ../model/src/main/resources/spicegen_darwin_arm64
	cp ./dist/spicegen_darwin_amd64*/spicegen_darwin_amd64* ../model/src/main/resources/spicegen_darwin_amd64
	cp ./dist/spicegen_linux_amd64*/spicegen_linux_amd64*     ../model/src/main/resources/spicegen_linux_amd64
	cp ./dist/spicegen_linux_arm64*/spicegen_linux_arm64*     ../model/src/main/resources/spicegen_linux_arm64
	cp ./dist/spicegen_linux_386*/spicegen_linux_386*         ../model/src/main/resources/spicegen_linux_386

fmt:
	gofumpt -w .

test:
	go test -v ./...

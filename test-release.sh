#!/bin/bash
# SPDX-License-Identifier: Apache-2.0 OR EUPL-1.2
#
# Test script for jfmt release process (dry-run mode)
# This script simulates the release workflow without making actual changes

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly NC='\033[0m' # No Color

GH_AVAILABLE=false

# Helper functions
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

check_git_repository() {
    git rev-parse --git-dir > /dev/null 2>&1 && return 0
    print_error "Not in a git repository"
    exit 1
}

check_project_root() {
    [ -f "pom.xml" ] && [ -d ".github/workflows" ] && return 0
    print_error "Not in project root directory"
    exit 1
}

check_maven_wrapper() {
    [ -x ./mvnw ] && return 0
    print_error "Maven wrapper (mvnw) not found or not executable"
    exit 1
}

check_github_cli() {
    if command -v gh &> /dev/null; then
        GH_AVAILABLE=true
        print_success "GitHub CLI available"
        return 0
    fi
    
    print_warning "GitHub CLI (gh) not found - artifact download will be skipped"
    return 0
}

check_gpg_signing_key() {
    if git config user.signingkey > /dev/null 2>&1; then
        print_success "GPG signing key configured"
        return 0
    fi
    
    print_warning "GPG signing key not configured in git"
    echo "  Run: git config --global user.signingkey <your-key-id>"
    return 0
}

check_gpg_commit_signing() {
    local gpg_commit_sign
    gpg_commit_sign="$(git config commit.gpgsign || echo "false")"
    
    if [ "$gpg_commit_sign" = "true" ]; then
        print_success "GPG commit signing enabled"
        return 0
    fi
    
    print_warning "GPG commit signing not enabled"
    echo "  Run: git config --global commit.gpgsign true"
    return 0
}

check_gpg_tag_signing() {
    local gpg_tag_sign
    gpg_tag_sign="$(git config tag.gpgsign || echo "false")"
    
    if [ "$gpg_tag_sign" = "true" ]; then
        print_success "GPG tag signing enabled"
        return 0
    fi
    
    print_warning "GPG tag signing not enabled"
    echo "  Run: git config --global tag.gpgsign true"
    return 0
}

check_prerequisites() {
    echo "============================================"
    echo "jfmt Release Process - Dry Run Test"
    echo "============================================"
    echo ""
    echo "Checking prerequisites..."
    
    check_git_repository
    print_success "Git repository detected"
    
    check_project_root
    print_success "In project root directory"
    
    check_maven_wrapper
    print_success "Maven wrapper available"
    
    check_github_cli
    check_gpg_signing_key
    check_gpg_commit_signing
    check_gpg_tag_signing
}

check_maven_release_plugin() {
    ./mvnw help:effective-pom -q | grep -q "maven-release-plugin" && return 0
    print_error "maven-release-plugin not found in POM"
    exit 1
}

check_scm_configuration() {
    if ./mvnw help:effective-pom -q | grep -A 5 "<scm>" | grep -q "<tag>"; then
        print_success "SCM tag configuration found"
        return 0
    fi
    
    print_warning "SCM tag configuration might be missing"
    return 0
}

test_release_prepare_dry_run() {
    echo ""
    echo "Testing release:prepare (dry-run)..."
    echo "This will simulate the release without making changes..."
    echo ""
    
    ./mvnw release:prepare -DdryRun=true -DskipTests=true 2>&1 | tee /tmp/release-prepare-dry-run.log || {
        print_error "release:prepare dry-run failed"
        echo "Check /tmp/release-prepare-dry-run.log for details"
        exit 1
    }
    
    print_success "release:prepare dry-run successful"
    
    if [ -f "release.properties" ]; then
        echo ""
        echo "Release properties that would be used:"
        grep -E "^(scm.tag|project.rel|project.dev)" release.properties || true
    fi
}

test_maven_release_phase() {
    echo ""
    echo "============================================"
    echo "Phase 1: Maven Release Configuration Test"
    echo "============================================"
    echo ""
    
    echo "Checking maven-release-plugin configuration..."
    check_maven_release_plugin
    print_success "maven-release-plugin is configured"
    
    echo "Checking SCM configuration..."
    check_scm_configuration
    
    test_release_prepare_dry_run
    
    echo ""
    echo "Cleaning up dry-run files..."
    ./mvnw release:clean -q
}

check_workflow_file_exists() {
    [ -f ".github/workflows/build-release-artifacts.yml" ] && return 0
    print_error "build-release-artifacts.yml workflow not found"
    exit 1
}

check_workflow_syntax() {
    command -v yamllint &> /dev/null || {
        print_warning "yamllint not available, skipping YAML validation"
        return 0
    }
    
    yamllint .github/workflows/build-release-artifacts.yml 2>&1 | grep -q "error" && {
        print_error "Workflow YAML has syntax errors"
        exit 1
    }
    
    print_success "Workflow YAML syntax valid"
    return 0
}

check_workflow_triggers() {
    grep -q "tags:" .github/workflows/build-release-artifacts.yml && \
    grep -q "'v\*'" .github/workflows/build-release-artifacts.yml && {
        print_success "Workflow triggers on v* tags"
        return 0
    }
    
    print_warning "Workflow tag trigger might not be configured correctly"
    return 0
}

test_ci_workflow_phase() {
    echo ""
    echo "============================================"
    echo "Phase 2: CI Workflow Validation"
    echo "============================================"
    echo ""
    
    check_workflow_file_exists
    print_success "build-release-artifacts.yml workflow exists"
    
    check_workflow_syntax
    check_workflow_triggers
}

check_jreleaser_github_token() {
    if [ -z "${JRELEASER_GITHUB_TOKEN:-}" ]; then
        print_warning "JRELEASER_GITHUB_TOKEN not set"
        echo "  Export it or add to ~/.bashrc: export JRELEASER_GITHUB_TOKEN=<token>"
        return 0
    fi
    
    print_success "JRELEASER_GITHUB_TOKEN is set"
    return 0
}

check_jreleaser_yml_exists() {
    [ -f "jreleaser.yml" ] && return 0
    print_error "jreleaser.yml not found"
    exit 1
}

test_jreleaser_configuration() {
    echo ""
    echo "Testing JReleaser configuration..."
    
    ./mvnw -N -Prelease jreleaser:config -q 2>&1 | tee /tmp/jreleaser-config.log || {
        print_error "JReleaser configuration has errors"
        echo "Check /tmp/jreleaser-config.log for details"
        exit 1
    }
    
    print_success "JReleaser configuration valid"
}

create_mock_artifacts() {
    local mock_dir="$1"
    
    mkdir -p "$mock_dir"
    
    # Regular release artifacts
    touch "$mock_dir/jfmt-0.1.0.zip"
    touch "$mock_dir/jfmt-0.1.0.tar.gz"
    touch "$mock_dir/jfmt-0.1.0-linux-x86_64.zip"
    touch "$mock_dir/jfmt-0.1.0-linux-x86_64.tar.gz"
    touch "$mock_dir/jfmt-0.1.0-windows-x86_64.zip"
    touch "$mock_dir/jfmt-0.1.0-osx-x86_64.zip"
    touch "$mock_dir/jfmt-0.1.0-osx-aarch_64.zip"
    
    # RC version artifacts (SemVer 2.0.0 format)
    touch "$mock_dir/jfmt-0.1.0-rc.1.zip"
    touch "$mock_dir/jfmt-0.1.0-rc.1.tar.gz"
    touch "$mock_dir/jfmt-0.1.0-rc.1-linux-x86_64.zip"
    touch "$mock_dir/jfmt-0.1.0-rc.1-linux-x86_64.tar.gz"
    touch "$mock_dir/jfmt-0.1.0-rc.1-windows-x86_64.zip"
    touch "$mock_dir/jfmt-0.1.0-rc.1-osx-x86_64.zip"
    touch "$mock_dir/jfmt-0.1.0-rc.1-osx-aarch_64.zip"
}

test_jreleaser_with_mock_artifacts() {
    local mock_artifacts="/tmp/jfmt-test-artifacts-$$"
    
    echo ""
    echo "Creating mock artifacts for testing..."
    create_mock_artifacts "$mock_artifacts"
    
    echo ""
    echo "Testing JReleaser with mock artifacts (dry-run)..."
    
    if ./mvnw -N -Prelease -DartifactsDir="$mock_artifacts" jreleaser:config -Djreleaser.dry.run=true 2>&1 | tee /tmp/jreleaser-artifacts.log; then
        print_success "JReleaser artifacts configuration valid"
    fi
    
    if ! ./mvnw -N -Prelease -DartifactsDir="$mock_artifacts" jreleaser:config -Djreleaser.dry.run=true 2>&1 | tee /tmp/jreleaser-artifacts.log; then
        print_warning "JReleaser might have issues with artifact paths"
        echo "Check /tmp/jreleaser-artifacts.log for details"
    fi
    
    rm -rf "$mock_artifacts"
}

test_jreleaser_phase() {
    echo ""
    echo "============================================"
    echo "Phase 3: JReleaser Configuration Test"
    echo "============================================"
    echo ""
    
    check_jreleaser_github_token
    
    check_jreleaser_yml_exists
    print_success "jreleaser.yml exists"
    
    test_jreleaser_configuration
    test_jreleaser_with_mock_artifacts
}

print_summary() {
    echo ""
    echo "============================================"
    echo "Summary"
    echo "============================================"
    echo ""
    print_success "All dry-run tests completed successfully!"
    echo ""
    echo "Your release process is ready. To perform an actual release:"
    echo ""
    echo "1. Phase 1: Create release tag"
    echo "   ./mvnw release:prepare release:perform"
    echo ""
    echo "2. Phase 2: Wait for CI to build artifacts"
    echo "   Monitor at: https://github.com/bmarwell/jfmt/actions"
    echo ""
    echo "3. Phase 3: Download and release"
    
    if [ "$GH_AVAILABLE" = true ]; then
        echo "   gh run download <run-id> -n release-artifacts-v<version> -D artifacts/"
    fi
    
    echo "   ./mvnw -N -Prelease -DartifactsDir=artifacts jreleaser:full-release"
    echo ""
    echo "For Release Candidates, use SemVer 2.0.0 format: 0.1.0-rc.1"
    echo "See RELEASING.adoc for full documentation."
    echo ""
}

main() {
    check_prerequisites
    test_maven_release_phase
    test_ci_workflow_phase
    test_jreleaser_phase
    print_summary
}

main

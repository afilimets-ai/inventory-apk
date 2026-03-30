# CI/CD Pipeline with GitHub Actions & Fastlane

Configure automated CI/CD: GitHub Actions workflow for build, lint (ktlint/detekt), and test on every PR. Fastlane lanes for debug and release APK generation. Automated signing configuration for release builds. Artifact upload for generated APKs. Foundation for per-flavor builds in Phase 2.

## Rationale
CI/CD is specified in the tech stack and is critical for maintaining code quality as the codebase grows. With white-label Product Flavors coming in Phase 2, the pipeline needs to support building multiple APK variants. Automated quality gates prevent regressions.

## User Stories
- As a developer, I want automated builds and lint checks on every PR so that code quality issues are caught before merge
- As the project owner, I want release APKs generated automatically so that deployment to Ndevor is streamlined

## Acceptance Criteria
- [ ] GitHub Actions workflow triggers on push to main and on PR creation
- [ ] Workflow runs: compile, lint (ktlint or detekt), unit tests
- [ ] Fastlane configured with at least 'debug' and 'release' lanes
- [ ] Release APK signed with proper keystore (secrets in GitHub)
- [ ] Build artifacts (APK) uploaded and downloadable from workflow
- [ ] Build status badge available for README
- [ ] Pipeline completes in < 10 minutes

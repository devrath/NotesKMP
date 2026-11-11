package com.istudio.noteskmp.feature.search.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.istudio.noteskmp.core.domain.AppDispatchers
import com.istudio.noteskmp.core.domain.DataError
import com.istudio.noteskmp.core.testing.FakeClock
import com.istudio.noteskmp.core.testing.FakeNoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val repository = FakeNoteRepository(FakeClock())
    private val store = ViewModelStore()

    @AfterTest
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(handle: SavedStateHandle = SavedStateHandle()): SearchViewModel {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        return SearchViewModel(handle, repository, FakeClock(), AppDispatchers(dispatcher)).also { store.put("vm", it) }
    }

    private fun TestScope.type(vm: SearchViewModel, query: String) {
        vm.onAction(SearchAction.OnQueryChange(query))
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS + 1)
    }

    @Test
    fun `nothing typed shows the start state`() = runTest {
        val vm = viewModel()

        assertThat(vm.state.value.isQueryEmpty).isTrue()
        assertThat(vm.state.value.hasNoResults).isFalse()
    }

    @Test
    fun `results wait for the debounce`() = runTest {
        repository.add("Weekly plan", "body")
        val vm = viewModel()
        vm.onAction(SearchAction.OnQueryChange("plan"))
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS - 1)
        assertThat(vm.state.value.results).isEmpty()

        advanceTimeBy(2)
        assertThat(vm.state.value.results.map { it.id }).containsExactly(1L)
    }

    @Test
    fun `matches are highlighted in title and snippet`() = runTest {
        repository.add("Weekly plan", "We agreed the **plan** keeps export")
        val vm = viewModel()
        type(vm, "PLAN")

        val result = vm.state.value.results.single()
        assertThat(result.titleMatches).containsExactly(7..10)
        assertThat(result.snippet.text).isEqualTo("We agreed the plan keeps export")
        assertThat(result.snippet.matches).containsExactly(14..17)
    }

    @Test
    fun `no results is reported only after the search for that query ran`() = runTest {
        repository.add("Plan", "")
        val vm = viewModel()
        vm.onAction(SearchAction.OnQueryChange("budget"))
        assertThat(vm.state.value.hasNoResults).isFalse()

        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS + 1)
        assertThat(vm.state.value.hasNoResults).isTrue()
    }

    @Test
    fun `punctuation-only query counts as empty`() = runTest {
        val vm = viewModel()
        type(vm, "** ##")

        assertThat(vm.state.value.isQueryEmpty).isTrue()
    }

    @Test
    fun `clearing the query clears the results`() = runTest {
        repository.add("Plan", "")
        val vm = viewModel()
        type(vm, "plan")
        type(vm, "")

        assertThat(vm.state.value.results).isEmpty()
    }

    @Test
    fun `query is kept in saved state and restored`() = runTest {
        repository.add("Plan", "")
        val handle = SavedStateHandle()
        type(viewModel(handle), "pla")
        store.clear()

        val restored = viewModel(handle)
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS + 1)
        assertThat(restored.state.value.query).isEqualTo("pla")
        assertThat(restored.state.value.results.size).isEqualTo(1)
    }

    @Test
    fun `storage error is shown instead of results`() = runTest {
        repository.error = DataError.Local.UNKNOWN
        val vm = viewModel()
        type(vm, "plan")

        assertThat(vm.state.value.error).isNotNull()
        assertThat(vm.state.value.hasNoResults).isTrue()
    }
}

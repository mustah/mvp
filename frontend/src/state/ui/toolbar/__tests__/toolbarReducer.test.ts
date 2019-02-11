import {changeToolbarView} from '../toolbarActions';
import {ToolbarState, ToolbarView} from '../toolbarModels';
import {initialState, toolbar} from '../toolbarReducer';

describe('toolbarReducer', () => {

  describe('changeView', () => {

    it('does not change view when same view', () => {
      const state: ToolbarState = toolbar(initialState, changeToolbarView(ToolbarView.graph));

      expect(state).toEqual(initialState);
    });

    it('changes view to table view', () => {
      const measurement = {view: ToolbarView.table};
      const state: ToolbarState = toolbar(initialState, changeToolbarView(ToolbarView.table));

      const expected: ToolbarState = {...initialState, measurement};
      expect(state).toEqual(expected);
    });

    it('changes view back and forth', () => {
      const measurement = {view: ToolbarView.table};
      const state: ToolbarState = toolbar(initialState, changeToolbarView(ToolbarView.table));

      const expected: ToolbarState = {...initialState, measurement};
      expect(state).toEqual(expected);

      const graphView = {view: ToolbarView.graph};
      const expectedGraphViewSate: ToolbarState = {...initialState, measurement: graphView};

      const newState: ToolbarState = toolbar(state, changeToolbarView(ToolbarView.graph));
      expect(newState).toEqual(expectedGraphViewSate);
    });
  });

});

import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {
  closeSelectionPage,
  resetSelection,
  saveSelection, selectSavedSelection,
  updateSelection,
} from '../../../state/search/selection/selectionActions';
import {OnSelectSelection, SelectionState} from '../../../state/search/selection/selectionModels';
import {getSelection} from '../../../state/search/selection/selectionSelectors';
import {OnClick, uuid} from '../../../types/Types';
import {IconNavigationBack} from '../../../components/icons/IconNavigationBack';
import {RowCenter, RowMiddle} from '../../../components/layouts/row/Row';
import {InlineEditInput} from '../components/selection-menu/InlineEditInput';

interface StateToProps {
  selection: SelectionState;
}

interface DispatchToProps {
  closeSelectionPage: OnClick;
  saveSelection: OnSelectSelection;
  updateSelection: OnSelectSelection;
  resetSelection: OnClick;
  selectSavedSelection: (id: uuid) => void;
}

export const SelectionMenu = (props: StateToProps & DispatchToProps) => {
  const {closeSelectionPage, selection, saveSelection, updateSelection, resetSelection, selectSavedSelection} = props;
  const key = `${selection.id}-${selection.isChanged}`;
  return (
    <RowCenter>
      <IconNavigationBack onClick={closeSelectionPage}/>
      <RowMiddle>
        <InlineEditInput
          key={key}
          isChanged={selection.isChanged}
          selection={selection}
          saveSelection={saveSelection}
          updateSelection={updateSelection}
          resetSelection={resetSelection}
          selectSavedSelection={selectSavedSelection}
        />
      </RowMiddle>
    </RowCenter>
  );
};

const mapStateToProps = ({searchParameters}: RootState): StateToProps => {
  return {
    selection: getSelection(searchParameters),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  closeSelectionPage,
  saveSelection,
  updateSelection,
  resetSelection,
  selectSavedSelection,
}, dispatch);

export const SelectionMenuContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SelectionMenu);

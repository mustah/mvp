import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {IconNavigationBack} from '../../../components/icons/IconNavigationBack';
import {RowCenter, RowMiddle} from '../../../components/layouts/row/Row';
import {RootState} from '../../../reducers/rootReducer';
import {
  closeSelectionPage,
  resetSelection,
  saveSelection,
  selectSavedSelection,
  updateSelection,
} from '../../../state/user-selection/userSelectionActions';
import {OnSelectSelection, UserSelection} from '../../../state/user-selection/userSelectionModels';
import {getUserSelection} from '../../../state/user-selection/userSelectionSelectors';
import {OnClick, OnClickWithId} from '../../../types/Types';
import {InlineEditInput} from '../components/selection-menu/InlineEditInput';

interface StateToProps {
  selection: UserSelection;
}

interface DispatchToProps {
  closeSelectionPage: OnClick;
  saveSelection: OnSelectSelection;
  updateSelection: OnSelectSelection;
  resetSelection: OnClick;
  selectSavedSelection: OnClickWithId;
}

export const SelectionMenu = (props: StateToProps & DispatchToProps) => {
  const {
    closeSelectionPage,
    selection,
    saveSelection,
    updateSelection,
    resetSelection,
    selectSavedSelection,
  } = props;

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

const mapStateToProps = ({userSelection}: RootState): StateToProps => ({
  selection: getUserSelection(userSelection),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  closeSelectionPage,
  saveSelection,
  updateSelection,
  resetSelection,
  selectSavedSelection,
}, dispatch);

export const SelectionMenuContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SelectionMenu);

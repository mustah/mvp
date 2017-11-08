import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {closeSearch, saveSelection, updateSelection} from '../../../state/search/selection/selectionActions';
import {OnSelectSelection, SelectionState} from '../../../state/search/selection/selectionModels';
import {getSelection} from '../../../state/search/selection/selectionSelectors';
import {OnClick} from '../../../types/Types';
import {CloseIcon} from '../../common/components/icons/IconClose';
import {RowCenter, RowMiddle} from '../../common/components/layouts/row/Row';
import {InlineEditInput} from '../components/selection-menu/InlineEditInput';

interface StateToProps {
  selection: SelectionState;
}

interface DispatchToProps {
  closeSearch: OnClick;
  saveSelection: OnSelectSelection;
  updateSelection: OnSelectSelection;
}

export const SelectionMenu = (props: StateToProps & DispatchToProps) => {
  const {closeSearch, selection, saveSelection, updateSelection} = props;
  return (
    <RowCenter>
      <CloseIcon onClick={closeSearch}/>
      <RowMiddle>
        <InlineEditInput
          key={selection.id}
          selection={selection}
          saveSelection={saveSelection}
          updateSelection={updateSelection}
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

const mapDispatchToProps = dispatch => bindActionCreators({
  closeSearch,
  saveSelection,
  updateSelection,
}, dispatch);

export const SelectionMenuContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(SelectionMenu);

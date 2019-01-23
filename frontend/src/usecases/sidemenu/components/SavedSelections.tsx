import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {routes} from '../../../app/routes';
import {listItemStyle, listItemStyleSelected, secondaryBgHover} from '../../../app/themes';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {Column} from '../../../components/layouts/column/Column';
import {Row, RowMiddle, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {Normal} from '../../../components/texts/Texts';
import {history} from '../../../index';
import {translate} from '../../../services/translationService';
import {UserSelection} from '../../../state/user-selection/userSelectionModels';
import {Callback, OnClickWithId, uuid} from '../../../types/Types';
import {DispatchToProps, StateToProps} from '../containers/SavedSelectionsContainer';
import {SelectionTreeContainer} from '../containers/SelectionTreeContainer';
import {LoadingTreeViewItems} from './LoadingTreeViewItems';
import {SavedSelectionActionsDropdown} from './SavedSelectionActionsDropdown';
import './SavedSelections.scss';

interface State {
  isDeleteDialogOpen: boolean;
  selectionToDelete?: uuid;
}

const innerDivStyle: React.CSSProperties = {
  padding: 0,
};

interface Props extends StateToProps, DispatchToProps {
  confirmDelete: OnClickWithId;
}

const ListItems = ({
  confirmDelete,
  isFetching,
  fetchUserSelections,
  savedSelections,
  selectSavedSelection,
  selection
}: Props) => {
  React.useEffect(() => fetchUserSelections(), [savedSelections]);

  const renderListItem = (id: uuid) => {
    const item: UserSelection = savedSelections.entities[id];
    const onAddAllToReport: Callback = () => {
      history.push(routes.report);
      selectSavedSelection(item.id);
    };
    const onSelectSelection: Callback = () => selectSavedSelection(item.id);
    const selectAndNavigateToMeters: Callback = () => {
      if (item.id !== selection.id) {
        history.push(routes.meter);
        selectSavedSelection(item.id);
      }
    };

    return (
      <Column key={`saved-${item.id}`}>
        <ListItem
          className="UserSelection-ListItem"
          style={item.id === selection.id ? listItemStyleSelected : listItemStyle}
          innerDivStyle={innerDivStyle}
          hoverColor={secondaryBgHover}
          value={item}
          key={item.id}
        >
          <RowSpaceBetween>
            <RowMiddle className="UserSelectionName flex-1" onClick={selectAndNavigateToMeters}>
              <Normal>{item.name}</Normal>
            </RowMiddle>
            <Row className="UserSelectionAction">
              <SavedSelectionActionsDropdown
                id={item.id}
                confirmDelete={confirmDelete}
                onAddAllToReport={onAddAllToReport}
                onSelectSelection={onSelectSelection}
              />
            </Row>
          </RowSpaceBetween>
        </ListItem>
        {item.id === selection.id && <SelectionTreeContainer/>}
      </Column>
    );
  };

  const items = savedSelections.result.length
    ? savedSelections.result.map(renderListItem)
    : [
      (
        <LoadingTreeViewItems
          isFetching={isFetching}
          text={translate('no saved selections')}
          key="loading-list-item"
        />
      )
    ];
  return <>{items}</>;
};

export class SavedSelections extends React.Component<Props, State> {

  state: State = {isDeleteDialogOpen: false};

  render() {
    return (
      <>
        <ListItems {...this.props} confirmDelete={this.openDialog}/>

        <ConfirmDialog
          isOpen={this.state.isDeleteDialogOpen}
          close={this.closeDialog}
          confirm={this.deleteSelectedUser}
        />
      </>
    );
  }

  openDialog = (id: uuid) => this.setState({isDeleteDialogOpen: true, selectionToDelete: id});

  closeDialog = () => this.setState({isDeleteDialogOpen: false});

  deleteSelectedUser = () => this.props.deleteUserSelection(this.state.selectionToDelete!);
}

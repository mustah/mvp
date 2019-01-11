import Divider from 'material-ui/Divider';
import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {
  dividerStyle,
  menuItemStyle,
  listItemStyleSelected,
  listStyle,
  nestedListItemStyle,
  sideBarHeaderStyle,
  sideBarStyle,
} from '../../../../app/themes';
import {ConfirmDialog} from '../../../../components/dialog/DeleteConfirmDialog';
import {Row} from '../../../../components/layouts/row/Row';
import {RootState} from '../../../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../../../services/translationService';
import {NormalizedState} from '../../../../state/domain-models/domainModels';
import {
  deleteUserSelection,
  fetchUserSelections,
  selectSavedSelection,
} from '../../../../state/user-selection/userSelectionActions';
import {UserSelection} from '../../../../state/user-selection/userSelectionModels';
import {getUserSelection} from '../../../../state/user-selection/userSelectionSelectors';
import {Callback, CallbackWithId, OnClick, uuid} from '../../../../types/Types';
import {LoadingListItem} from '../../components/LoadingListItem';
import {SavedSelectionActionsDropdown} from '../../components/saved-selections/SavedSelectionActionsDropdown';
import './SavedSelectionsContainer.scss';

interface StateToProps {
  selection: UserSelection;
  savedSelections: NormalizedState<UserSelection>;
  isFetching: boolean;
}

interface DispatchToProps {
  deleteUserSelection: CallbackWithId;
  fetchUserSelections: Callback;
  selectSavedSelection: OnClick;
}

interface State {
  isDeleteDialogOpen: boolean;
  selectionToDelete?: uuid;
}

const innerDivStyle: React.CSSProperties = {padding: 0};

class SavedSelections extends React.Component<StateToProps & DispatchToProps, State> {

  state: State = {isDeleteDialogOpen: false};

  componentDidMount() {
    this.props.fetchUserSelections();
  }

  render() {
    const {isFetching, savedSelections: {entities, result}, selectSavedSelection, selection} = this.props;

    const renderListItem = (id: uuid) => {
      const item: UserSelection = entities[id];
      const onSelectSelection: Callback = () => selectSavedSelection(item.id);
      const style: React.CSSProperties = item.id === selection.id
        ? listItemStyleSelected
        : menuItemStyle;

      return (
        <ListItem
          style={style}
          innerDivStyle={innerDivStyle}
          hoverColor={sideBarStyle.color}
          value={item}
          key={item.id}
        >
          <Row className="space-between">
            <Row className="UserSelectionName flex-1" onClick={onSelectSelection}>
              {item.name}
            </Row>
            <Row className="UserSelectionAction">
              <SavedSelectionActionsDropdown id={item.id} openConfirmDialog={this.openDialog}/>
            </Row>
          </Row>
        </ListItem>
      );
    };

    const listItems = result.length
      ? result.map(renderListItem)
      : [(
           <LoadingListItem
             isFetching={isFetching}
             text={translate('no saved selections')}
             key="loading-list-item"
           />
         )];

    return (
      <List style={listStyle}>
        <ListItem
          className="ListItem"
          primaryText={firstUpperTranslated('saved selections')}
          initiallyOpen={true}
          style={sideBarHeaderStyle}
          hoverColor={sideBarStyle.color}
          nestedItems={listItems}
          nestedListStyle={nestedListItemStyle}
        />
        <Divider style={dividerStyle}/>
        <ConfirmDialog
          isOpen={this.state.isDeleteDialogOpen}
          close={this.closeDialog}
          confirm={this.deleteSelectedUser}
        />
      </List>
    );
  }

  openDialog = (id: uuid) => this.setState({isDeleteDialogOpen: true, selectionToDelete: id});

  closeDialog = () => this.setState({isDeleteDialogOpen: false});

  deleteSelectedUser = () => this.props.deleteUserSelection(this.state.selectionToDelete!);
}

const mapStateToProps =
  ({userSelection, domainModels: {userSelections}}: RootState): StateToProps =>
    ({
      selection: getUserSelection(userSelection),
      savedSelections: userSelections,
      isFetching: userSelections.isFetching,
    });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectSavedSelection,
  fetchUserSelections,
  deleteUserSelection,
}, dispatch);

export const SavedSelectionsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SavedSelections);

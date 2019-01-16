import Divider from 'material-ui/Divider';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {routes} from '../../../../app/routes';
import {colors, dividerStyle, listItemStyle, listItemStyleSelected} from '../../../../app/themes';
import {ConfirmDialog} from '../../../../components/dialog/DeleteConfirmDialog';
import {FoldableMenuItem} from '../../../../components/layouts/foldable/FoldableMenuItem';
import {Row} from '../../../../components/layouts/row/Row';
import {Normal} from '../../../../components/texts/Texts';
import {history} from '../../../../index';
import {RootState} from '../../../../reducers/rootReducer';
import {translate} from '../../../../services/translationService';
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

const innerDivStyle: React.CSSProperties = {
  padding: 0,
};

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
      const navigateToMeters: Callback = () => {
        history.push(routes.meter);
        selectSavedSelection(item.id);
      };

      return (
        <ListItem
          className="UserSelection-ListItem"
          style={item.id === selection.id ? listItemStyleSelected : listItemStyle}
          innerDivStyle={innerDivStyle}
          hoverColor={colors.listItemHover}
          value={item}
          key={item.id}
        >
          <Row className="space-between">
            <Row className="UserSelectionName flex-1" onClick={navigateToMeters}>
              <Normal>{item.name}</Normal>
            </Row>
            <Row className="UserSelectionAction">
              <SavedSelectionActionsDropdown
                id={item.id}
                confirmDelete={this.openDialog}
                onSelectSelection={onSelectSelection}
              />
            </Row>
          </Row>
        </ListItem>
      );
    };

    const listItems = result.length
      ? result.map(renderListItem)
      : [
        (
          <LoadingListItem
            isFetching={isFetching}
            text={translate('no saved selections')}
            key="loading-list-item"
          />
        )
      ];

    return (
      <>
        <FoldableMenuItem title={translate('saved selections')}>
          {listItems}
        </FoldableMenuItem>

        <Divider style={dividerStyle}/>

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

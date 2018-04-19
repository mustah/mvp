import Divider from 'material-ui/Divider';
import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {
  dividerStyle,
  listItemStyle,
  listItemStyleSelected,
  listStyle,
  nestedListItemStyle,
  sideBarHeaderStyle,
  sideBarStyles,
} from '../../../../app/themes';
import {ConfirmDialog} from '../../../../components/dialog/DeleteConfirmDialog';
import {Row} from '../../../../components/layouts/row/Row';
import {RootState} from '../../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../../services/translationService';
import {NormalizedState} from '../../../../state/domain-models/domainModels';
import {
  deleteUserSelection,
  fetchUserSelections,
  selectSavedSelection,
} from '../../../../state/search/selection/selectionActions';
import {UserSelection} from '../../../../state/search/selection/selectionModels';
import {getSelection} from '../../../../state/search/selection/selectionSelectors';
import {Callback, CallbackSingle, OnClick, uuid} from '../../../../types/Types';
import {NoSavedSelections} from '../../components/savedSelections/NoSavedSelections';
import {SavedSelectionActionsDropdown} from '../../components/savedSelections/SavedSelectionActionsDropdown';

interface StateToProps {
  selection: UserSelection;
  savedSelections: NormalizedState<UserSelection>;
}

interface DispatchToProps {
  deleteUserSelection: CallbackSingle;
  fetchUserSelections: Callback;
  selectSavedSelection: OnClick;
}

interface State {
  isDeleteDialogOpen: boolean;
  selectionToDelete?: uuid;
}

class SavedSelections extends React.Component<StateToProps & DispatchToProps, State> {

  state: State = {isDeleteDialogOpen: false};
  openDialog = (id: uuid) => this.setState({isDeleteDialogOpen: true, selectionToDelete: id});
  closeDialog = () => this.setState({isDeleteDialogOpen: false});
  deleteSelectedUser = () => this.props.deleteUserSelection(this.state.selectionToDelete!);

  componentDidMount() {
    this.props.fetchUserSelections();
  }

  render() {
    const {savedSelections, selectSavedSelection, selection} = this.props;

    const innerDivStyle: React.CSSProperties = {
      padding: 0,
    };

    const renderListItem = (id: uuid) => {
      const item: UserSelection = savedSelections.entities[id];
      const onSelectSelection: Callback = () => selectSavedSelection(item.id);
      const style: React.CSSProperties = item.id === selection.id
        ? listItemStyleSelected
        : listItemStyle;

      return (
        <ListItem
          style={style}
          innerDivStyle={innerDivStyle}
          hoverColor={sideBarStyles.onHover.color}
          value={item}
          key={item.id}
        >
          <Row className="space-between">
            <Row style={{paddingTop: '6px'}} className="flex-1" onClick={onSelectSelection}>
              {item.name}
            </Row>
            <Row style={{transform: 'scale(0.8)', paddingRight: '13px'}}>
              <SavedSelectionActionsDropdown id={item.id} confirmDelete={this.openDialog}/>
            </Row>
          </Row>
        </ListItem>
      );
    };

    const listItems = savedSelections.result.length
      ? savedSelections.result.map(renderListItem)
      : [<NoSavedSelections key={1}/>];

    return (
      <List style={listStyle}>
        <ListItem
          className="ListItem"
          primaryText={firstUpperTranslated('saved selections')}
          initiallyOpen={true}
          style={sideBarHeaderStyle}
          hoverColor={sideBarStyles.onHover.color}
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
}

const mapStateToProps = ({userSelection, domainModels: {userSelections}}: RootState): StateToProps => {
  return {
    selection: getSelection(userSelection),
    savedSelections: userSelections,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectSavedSelection,
  fetchUserSelections,
  deleteUserSelection,
}, dispatch);

export const SavedSelectionsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SavedSelections);

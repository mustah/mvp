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
import {RootState} from '../../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../../services/translationService';
import {NormalizedState} from '../../../../state/domain-models/domainModels';
import {fetchUserSelections, selectSavedSelection} from '../../../../state/search/selection/selectionActions';
import {UserSelection} from '../../../../state/search/selection/selectionModels';
import {getSelection} from '../../../../state/search/selection/selectionSelectors';
import {Callback, OnClick, uuid} from '../../../../types/Types';
import {NoSavedSelections} from '../../components/savedSelections/NoSavedSelections';

interface StateToProps {
  selection: UserSelection;
  savedSelections: NormalizedState<UserSelection>;
}

interface DispatchToProps {
  fetchUserSelections: Callback;
  selectSavedSelection: OnClick;
}

class SavedSelections extends React.Component<StateToProps & DispatchToProps> {

  componentDidMount() {
    this.props.fetchUserSelections();
  }

  render() {
    const {savedSelections, selectSavedSelection, selection} = this.props;

    const innerDivStyle: React.CSSProperties = {
      ...sideBarStyles.padding,
      ...sideBarStyles.selected,
    };

    const renderListItem = (id: uuid) => {
      const item: UserSelection = savedSelections.entities[id];
      const onSelectSelection: Callback = () => selectSavedSelection(item.id);
      const style: React.CSSProperties = item.id === selection.id
        ? listItemStyleSelected
        : listItemStyle;
      return (
        <ListItem
          onClick={onSelectSelection}
          style={style}
          innerDivStyle={innerDivStyle}
          hoverColor={sideBarStyles.onHover.color}
          primaryText={item.name}
          value={item}
          key={item.id}
        />
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
      </List>
    );
  }
}

const mapStateToProps = ({searchParameters, domainModels: {userSelections}}: RootState): StateToProps => {
  return {
    selection: getSelection(searchParameters),
    savedSelections: userSelections,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectSavedSelection,
  fetchUserSelections,
}, dispatch);

export const SavedSelectionsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SavedSelections);

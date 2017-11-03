import Divider from 'material-ui/Divider';
import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../reducers/rootReducer';
import {translate} from '../../../../services/translationService';
import {selectSavedSelection} from '../../../../state/search/selection/selectionActions';
import {IdNamed, OnClick} from '../../../../types/Types';
import {
  dividerStyle,
  listItemStyle,
  listStyle,
  nestedListItemStyle,
  sideBarHeaderStyle,
  sideBarStyles,
} from '../../../app/themes';

interface StateToProps {
  selections: IdNamed[];
}

interface DispatchToProps {
  selectSavedSelection: OnClick;
}

const SavedSelections = (props: StateToProps & DispatchToProps) => {
  const {selections, selectSavedSelection} = props;

  const innerDivStyle: React.CSSProperties = {
    ...sideBarStyles.padding,
    ...sideBarStyles.selected,
  };

  const renderListItem = (item: IdNamed) => {
    const onSelectSelection = () => selectSavedSelection(item.id);
    return (
      <ListItem
        onClick={onSelectSelection}
        style={listItemStyle}
        innerDivStyle={innerDivStyle}
        hoverColor={sideBarStyles.onHover.color}
        primaryText={item.name}
        value={item}
        key={item.id}
      />
    );
  };

  const listItems = selections.map(renderListItem);

  return (
    <List style={listStyle}>
      <ListItem
        className="ListItem"
        primaryText={translate('saved search')}
        initiallyOpen={true}
        style={sideBarHeaderStyle}
        hoverColor={sideBarStyles.onHover.color}
        nestedItems={listItems}
        nestedListStyle={nestedListItemStyle}
      />
      <Divider style={dividerStyle}/>
    </List>
  );
};

const mapStateToProps = ({ui}: RootState): StateToProps => {
  return {
    selections: [{name: 'Älmhult - Centrum', id: 1}, {name: 'Perstorp', id: 2}],
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  selectSavedSelection,
}, dispatch);

export const SavedSelectionsContainer =
  connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(SavedSelections);

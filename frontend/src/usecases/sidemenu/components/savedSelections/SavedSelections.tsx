import Divider from 'material-ui/Divider';
import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../reducers/rootReducer';
import {translate} from '../../../../services/translationService';
import {selectSavedSelection} from '../../../../state/search/selection/selectionActions';
import {getSavedSelections} from '../../../../state/search/selection/selectionSelectors';
import {IdNamed, OnClick} from '../../../../types/Types';
import {
  dividerStyle,
  listItemStyle,
  listStyle,
  nestedListItemStyle,
  sideBarHeaderStyle,
  sideBarStyles,
} from '../../../../app/themes';
import {NoSavedSelections} from './NoSavedSelections';

interface StateToProps {
  selections: IdNamed[];
  hasSelections: boolean;
}

interface DispatchToProps {
  selectSavedSelection: OnClick;
}

const SavedSelections = (props: StateToProps & DispatchToProps) => {
  const {hasSelections, selections, selectSavedSelection} = props;

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

  const listItems = hasSelections ? selections.map(renderListItem) : [<NoSavedSelections key={1}/>];

  return (
    <List style={listStyle}>
      <ListItem
        className="ListItem"
        primaryText={translate('saved selections')}
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

const mapStateToProps = ({searchParameters}: RootState): StateToProps => {
  const savedSelections = getSavedSelections(searchParameters);
  return {
    selections: savedSelections,
    hasSelections: savedSelections.length !== 0,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectSavedSelection,
}, dispatch);

export const SavedSelectionsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SavedSelections);

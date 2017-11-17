import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {SelectionTreeData} from '../../../../state/domain-models/meter/meterModels';
import {uuid} from '../../../../types/Types';
import {listItemStyle, nestedListItemStyle, sideBarStyles} from '../../../app/themes';
import ListItemProps = __MaterialUI.List.ListItemProps;

export class SelectableListItem extends React.Component<ListItemProps & {selectable: boolean}, {selected: boolean}> {

  state = {selected: false};

  render() {
    const {selectable, ...ListItemProps} = this.props;
    const selectableStyle: React.CSSProperties = selectable ? {} : sideBarStyles.notSelectable;
    const selectedStyle: React.CSSProperties = this.state.selected ? sideBarStyles.selected : selectableStyle;
    return (
      <ListItem
        {...ListItemProps}
        style={{...listItemStyle, ...selectedStyle}}
        hoverColor={sideBarStyles.onHover.color}
        onClick={selectable ? this.onClick : () => null}
      />
    );
  }

  onClick = (): void => {
    this.setState((prevState => ({selected: !prevState.selected})));
  }

}

interface RenderSelectionTreeFunctionProps {
  id: uuid;
  data: SelectionTreeData;
  level: string;
  toggleExpand: (id: uuid) => void;
  openListItems: Set<uuid>;
}

export const renderSelectionTree = (props: RenderSelectionTreeFunctionProps) => {
  const {id, data, level, toggleExpand, openListItems} = props;
  const entity = data.entities[level][id];
  const selectable = entity.selectable;

  const renderChildNodes = (itemId: uuid) => renderSelectionTree({
    id: itemId,
    data,
    level: entity.childNodes.type,
    toggleExpand,
    openListItems,
  });
  const nestedItems = entity.childNodes.ids.sort().map(renderChildNodes);

  const toggleOpen = () => toggleExpand(entity.id);

  return (
    <SelectableListItem
      className="TreeListItem"
      primaryText={entity.name}
      key={id}
      innerDivStyle={sideBarStyles.padding}
      initiallyOpen={openListItems.has(entity.id)}
      nestedItems={nestedItems}
      nestedListStyle={nestedListItemStyle}
      onNestedListToggle={toggleOpen}
      selectable={selectable}
    />
  );
};

import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {SelectionTreeData} from '../../../../state/domain-models/meter/meterModels';
import {uuid} from '../../../../types/Types';
import {listItemStyle, nestedListItemStyle, sideBarStyles} from '../../../app/themes';
import ListItemProps = __MaterialUI.List.ListItemProps;

export const SelectableListItem = (props: ListItemProps & {selectable: boolean; selected: boolean}) => {

  const {selectable, selected, ...ListItemProps} = props;
  const selectableStyle: React.CSSProperties = selectable ? {} : sideBarStyles.notSelectable;
  const selectedStyle: React.CSSProperties = selected ? sideBarStyles.selected : selectableStyle;
  return (
    <ListItem
      {...ListItemProps}
      style={{...listItemStyle, ...selectedStyle}}
      hoverColor={sideBarStyles.onHover.color}
    />
  );
};

interface RenderSelectionTreeFunctionProps {
  id: uuid;
  data: SelectionTreeData;
  level: string;
  toggleExpand: (id: uuid) => void;
  openListItems: Set<uuid>;
  toggleSelect: (id: uuid) => void;
  selectedListItems: Set<uuid>;
}

export const renderSelectionTree = (props: RenderSelectionTreeFunctionProps) => {
  const {id, data, level, toggleExpand, openListItems, toggleSelect, selectedListItems} = props;
  const entity = data.entities[level][id];
  const selectable = entity.selectable;

  const renderChildNodes = (id: uuid) => renderSelectionTree({
    id,
    data,
    level: entity.childNodes.type,
    toggleExpand,
    openListItems,
    toggleSelect,
    selectedListItems,
  });
  const nestedItems = entity.childNodes.ids.sort().map(renderChildNodes);

  const onToggleExpand = () => toggleExpand(entity.id);
  const onToggleSelect = () => toggleSelect(entity.id);
  const PrimaryText = <div style={listItemStyle.textStyle}>{entity.name}</div>;

  return (
    <SelectableListItem
      className="TreeListItem"
      primaryText={PrimaryText}
      key={id}
      innerDivStyle={sideBarStyles.padding}
      initiallyOpen={openListItems.has(entity.id)}
      nestedItems={nestedItems}
      nestedListStyle={nestedListItemStyle}
      onNestedListToggle={onToggleExpand}
      onClick={selectable ? onToggleSelect : () => null}
      selectable={selectable}
      selected={selectedListItems.has(entity.id)}
    />
  );
};

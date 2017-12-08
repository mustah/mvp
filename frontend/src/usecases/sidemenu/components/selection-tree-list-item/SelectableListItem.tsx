import {ListItem} from 'material-ui';
import * as React from 'react';
import {listItemStyle, sideBarStyles} from '../../../../app/themes';
import ListItemProps = __MaterialUI.List.ListItemProps;

interface Selectable {
  selectable: boolean;
  selected: boolean;
}

export const SelectableListItem = (props: ListItemProps & Selectable) => {
  const {selectable, selected, ...listItemProps} = props;
  const selectableStyle: React.CSSProperties = selectable ? {} : sideBarStyles.notSelectable;
  const selectedStyle: React.CSSProperties = selected ? sideBarStyles.selected : selectableStyle;
  const listStyle: React.CSSProperties = {...listItemStyle, ...selectedStyle};

  return (
    <ListItem
      {...listItemProps}
      style={listStyle}
      hoverColor={sideBarStyles.onHover.color}
    />
  );
};

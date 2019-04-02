import ListItem from 'material-ui/List/ListItem';
import ContentAdd from 'material-ui/svg-icons/content/add';
import * as React from 'react';
import {history, routes} from '../../../app/routes';
import {colors, listItemInnerDivStyle, listItemStyle, secondaryBgHover} from '../../../app/themes';
import {RowMiddle} from '../../../components/layouts/row/Row';
import {Medium} from '../../../components/texts/Texts';
import {firstUpperTranslated} from '../../../services/translationService';
import {OnClick} from '../../../types/Types';
import './SavedSelections.scss';

const iconStyle: React.CSSProperties = {
  marginLeft: 8,
  width: 20,
  height: 20,
  color: colors.lightBlack,
};

interface Props {
  resetSelection: OnClick;
}

export const CreateNewSelectionListItem = ({resetSelection}: Props) => {
  const onSelect = () => {
    resetSelection();
    history.push(routes.selection);
  };
  return (
    <ListItem
      className="SavedSelection-ListItem"
      style={listItemStyle}
      innerDivStyle={listItemInnerDivStyle}
      hoverColor={secondaryBgHover}
      key={`create-new-selection`}
    >
      <RowMiddle className="SavedSelection-Name flex-1" onClick={onSelect}>
        <ContentAdd hoverColor={colors.black} style={iconStyle}/>
        <Medium className="CreateNewSelection">{firstUpperTranslated('create new selection')}</Medium>
      </RowMiddle>
    </ListItem>
  );
};

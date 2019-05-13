import {important} from 'csx';
import ListItem from 'material-ui/List/ListItem';
import ContentAdd from 'material-ui/svg-icons/content/add';
import * as React from 'react';
import {classes, style} from 'typestyle';
import {colors} from '../../../app/colors';
import {history, routes} from '../../../app/routes';
import {listItemStyle} from '../../../app/themes';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {RowMiddle} from '../../../components/layouts/row/Row';
import {Medium} from '../../../components/texts/Texts';
import {firstUpperTranslated} from '../../../services/translationService';
import {OnClick} from '../../../types/Types';
import './SavedSelections.scss';

interface Props extends ThemeContext {
  resetSelection: OnClick;
}

export const CreateNewSelectionListItem = withCssStyles(({cssStyles: {primary, secondary}, resetSelection}: Props) => {
  const iconStyle: React.CSSProperties = {
    marginLeft: 8,
    width: 20,
    height: 20,
    color: primary.fg,
  };
  const onSelect = () => {
    resetSelection();
    history.push(routes.selection);
  };
  const className = style({$nest: {':hover svg': {fill: important(secondary.fgHover)}}});
  return (
    <ListItem
      className={classes('SavedSelection-ListItem', className)}
      style={listItemStyle}
      innerDivStyle={{padding: 0}}
      hoverColor={secondary.bgHover}
      key={`create-new-selection`}
    >
      <RowMiddle className="SavedSelection-Name flex-1" onClick={onSelect}>
        <ContentAdd hoverColor={colors.black} style={iconStyle}/>
        <Medium className="CreateNewSelection">{firstUpperTranslated('create new selection')}</Medium>
      </RowMiddle>
    </ListItem>
  );
});

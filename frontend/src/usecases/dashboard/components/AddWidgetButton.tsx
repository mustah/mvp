import {important} from 'csx';
import FlatButton from 'material-ui/FlatButton';
import Add from 'material-ui/svg-icons/content/add';
import * as React from 'react';
import {classes, style} from 'typestyle';
import {colors} from '../../../app/colors';
import {border} from '../../../app/themes';
import {ActionsDropdown} from '../../../components/actions-dropdown/ActionsDropdown';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {IconProps} from '../../../components/popover/PopoverMenu';
import {translate} from '../../../services/translationService';
import {OnClick, RenderFunction} from '../../../types/Types';
import './AddWidgetButton.scss';

interface Props extends ThemeContext {
  renderPopoverContent: RenderFunction<OnClick>;
}

export const AddWidgetButton = withCssStyles(({renderPopoverContent, cssStyles: {primary}}: Props) => {
  const className = style({
    $nest: {
      '&:hover button': {border: important(`1px solid ${primary.bg}`)},
      '&:hover span': {color: important(colors.black)},
      '&:hover svg': {fill: important(colors.black)},
    }
  });
  const iconProps: IconProps = {
    color: primary.fg,
    icon: <Add color={primary.fg}/>,
    hoverColor: primary.bgHover,
    backgroundColor: colors.white,
    label: translate('add new widget'),
    labelStyle: {color: primary.fg},
    style: {
      height: 40,
      borderRadius: 40 / 2,
      border,
      zIndex: 4,
    },
  };

  return (
    <ActionsDropdown
      className={classes('AddWidgetButton', className)}
      Icon={FlatButton}
      iconProps={iconProps}
      renderPopoverContent={renderPopoverContent}
    />
  );
});

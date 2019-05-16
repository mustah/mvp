import * as React from 'react';
import {Color} from '../../../../app/colors';
import {ButtonLinkRed} from '../../../../components/buttons/ButtonLink';
import {ButtonPrimary} from '../../../../components/buttons/ButtonPrimary';
import {ButtonSecondary} from '../../../../components/buttons/ButtonSecondary';
import {ThemeContext} from '../../../../components/hoc/withThemeProvider';
import {Row, RowBottom} from '../../../../components/layouts/row/Row';
import {PopoverWrapper} from '../../../../components/popover/PopoverWrapper';
import {firstUpperTranslated, translate} from '../../../../services/translationService';
import {OnClick, OnClickWith} from '../../../../types/Types';
import {Colors} from '../../../theme/themeReducer';
import {ColorPicker} from './ColorPicker';

export interface StateToProps {
  color: Colors;
}

export interface DispatchToProps {
  changePrimaryColor: OnClickWith<Color>;
  changeSecondaryColor: OnClickWith<Color>;
  resetColors: OnClick;
}

export type Props = StateToProps & DispatchToProps & ThemeContext;

export const ColorPickers = ({
  changePrimaryColor,
  changeSecondaryColor,
  resetColors,
  color: {primary, secondary}
}: Props) => {
  const renderPrimaryPicker = _ => <ColorPicker onChange={changePrimaryColor} color={primary}/>;
  const renderSecondaryPicker = _ => <ColorPicker onChange={changeSecondaryColor} color={secondary}/>;

  return (
    <Row>
      <Row style={{marginRight: 16}}>
        <PopoverWrapper renderPopoverContent={renderPrimaryPicker}>
          <div>
            <ButtonPrimary label={translate('primary color')}/>
          </div>
        </PopoverWrapper>
      </Row>
      <Row style={{marginRight: 16}}>
        <PopoverWrapper renderPopoverContent={renderSecondaryPicker}>
          <div>
            <ButtonSecondary label={translate('secondary color')}/>
          </div>
        </PopoverWrapper>
      </Row>
      <RowBottom>
        <ButtonLinkRed onClick={resetColors}>
          {firstUpperTranslated('reset')}
        </ButtonLinkRed>
      </RowBottom>
    </Row>
  );
};

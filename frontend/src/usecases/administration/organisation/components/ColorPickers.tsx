import * as React from 'react';
import {Color} from '../../../../app/colors';
import {ButtonLinkRed} from '../../../../components/buttons/ButtonLink';
import {ButtonPrimary} from '../../../../components/buttons/ButtonPrimary';
import {ButtonSecondary} from '../../../../components/buttons/ButtonSecondary';
import {withWidgetLoader} from '../../../../components/hoc/withLoaders';
import {ThemeContext} from '../../../../components/hoc/withThemeProvider';
import {Row, RowBottom} from '../../../../components/layouts/row/Row';
import {PopoverWrapper} from '../../../../components/popover/PopoverWrapper';
import {makeThemeUrlOf} from '../../../../helpers/urlFactory';
import {firstUpperTranslated, translate} from '../../../../services/translationService';
import {RequestsHttp} from '../../../../state/domain-models/domainModels';
import {Organisation} from '../../../../state/domain-models/organisation/organisationModels';
import {Omit, OnClick, OnFetch} from '../../../../types/Types';
import {Colors, OnChangeColor} from '../../../theme/themeModels';
import {ColorPicker} from './ColorPicker';

export interface StateToProps extends RequestsHttp {
  color: Colors;
}

export interface DispatchToProps {
  changePrimaryColor: OnChangeColor;
  changeSecondaryColor: OnChangeColor;
  fetchTheme: OnFetch;
  resetColors: OnClick;
}

export interface OwnProps {
  organisation: Organisation;
}

export type Props = StateToProps & DispatchToProps & OwnProps & ThemeContext;

const ColorPickersComponent = ({
  changePrimaryColor,
  changeSecondaryColor,
  organisation: {id},
  resetColors,
  color: {primary, secondary}
}: StateToProps & OwnProps & Omit<DispatchToProps, 'fetchTheme'>) => {
  const changePrimaryColorHandler = (color: Color) => changePrimaryColor(color, id);
  const changeSecondaryColorHandler = (color: Color) => changeSecondaryColor(color, id);

  const renderPrimaryPicker = _ => <ColorPicker onChange={changePrimaryColorHandler} color={primary}/>;
  const renderSecondaryPicker = _ => <ColorPicker onChange={changeSecondaryColorHandler} color={secondary}/>;

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

const LoadingColorPicker = withWidgetLoader(ColorPickersComponent);

export const ColorPickers = (props: Props) => {
  const {fetchTheme, isSuccessfullyFetched, organisation: {slug}} = props;
  React.useEffect(() => {
    fetchTheme(makeThemeUrlOf(slug));
  }, [isSuccessfullyFetched]);
  return <LoadingColorPicker {...props}/>;
};

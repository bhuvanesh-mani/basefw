package fw.components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class DropDown extends Select {
	private WebElement dropdown;
	
	public DropDown(WebElement dropdown) {
		super(dropdown);
		this.dropdown = dropdown;
	}
	
	public void selectByValue(String value, boolean caseSensitive) {
		if(caseSensitive) {
			super.selectByValue(value);
			return;
		}
		
		boolean isFound = false;
		for(WebElement option : dropdown.findElements(By.xpath(".//option"))) {
			String actValue = option.getText();
			if(actValue == null || actValue.trim().isEmpty())
				actValue = option.getAttribute("textContent");
			
			if(actValue == null || actValue.trim().isEmpty())
				continue;
			
			if(actValue.equalsIgnoreCase(value)) {
				super.selectByValue(actValue);
				isFound = true;
			}
			
			if(isFound)
				return;
		}
		
	}
}

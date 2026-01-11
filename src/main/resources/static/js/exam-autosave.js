// در بالای فایل یا در سطح جهانی
const csrfToken = document.querySelector('meta[name="_csrf"]').content;

class ExamAutoSave {

    constructor(participationId, saveInterval = 30000) {
        this.participationId = participationId;
        this.saveInterval = saveInterval;
        this.answers = new Map();
        this.saveIntervalId = null;
        this.isSaving = false;
    }

    start() {
        this.saveIntervalId = setInterval(() => {
            this.saveAllAnswers();
        }, this.saveInterval);
    }

    stop() {
        if (this.saveIntervalId) {
            clearInterval(this.saveIntervalId);
            this.saveIntervalId = null;
        }
    }

    setAnswer(questionId, answerData) {
        this.answers.set(questionId, answerData);
    }

    removeAnswer(questionId) {
        this.answers.delete(questionId);
    }

    async saveAllAnswers() {
        if (this.isSaving || this.answers.size === 0) return;

        this.isSaving = true;

        for (const [questionId, answerData] of this.answers.entries()) {
            try {
                await this.saveSingleAnswer(questionId, answerData);
                this.answers.delete(questionId);
            } catch (error) {
                console.error('Error saving answer:', error);
            }
        }

        this.isSaving = false;
    }


    async saveSingleAnswer(questionId, answerData) {
        const formData = new FormData();
        formData.append('questionId', questionId);

        // اضافه کردن selectedOptionId فقط اگر وجود داشته باشد
        // if (answerData.selectedOptionId !== undefined) {
        //     formData.append('selectedOptionId', answerData.selectedOptionId);
        // }

        if (answerData.selectedOptionId !== undefined && answerData.selectedOptionId !== null && !isNaN(answerData.selectedOptionId)) {
            formData.append('selectedOptionId', answerData.selectedOptionId); // ارسال عدد
        }

        if (answerData.answerText !== undefined && answerData.answerText !== null) {
            formData.append('answerText', answerData.answerText);
        }


        console.log("FormData Content (Length:", formData.entries().length, "):", [...formData.entries()]);

// console.log("FormData:", formData); // بررسی محتوای FormData




        const response = await fetch(`/student/exam/${this.participationId}/save-answer`, {
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': csrfToken // نام هدر استاندارد Spring Security
            },
            body: formData
        });

        // if (!response.ok) {
        //     throw new Error('Failed to save answer');
        // }
        //
        //
        //
        // return response.text();
        if (!response.ok) {
            // خواندن متن پاسخ سرور برای دیباگ کردن خطای 400 در سمت کلاینت
            const errorBody = await response.text();
            console.error(`Server returned ${response.status}: ${errorBody}`);
            throw new Error(`Failed to save answer. Server responded with status ${response.status}.`);
        }

        return response.text();
    }


    // بازیابی پاسخ‌های ذخیره شده
    async recoverAnswers() {
        try {
            const response = await fetch(`/student/exam/${this.participationId}/recover`);
            if (response.ok) {
                const data = await response.json();
                return data.answers || [];
            }
        } catch (error) {
            console.error('Error recovering answers:', error);
        }
        return [];
    }
}
